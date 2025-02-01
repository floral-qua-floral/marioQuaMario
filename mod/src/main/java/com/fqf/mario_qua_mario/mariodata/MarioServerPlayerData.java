package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.ParsedTransition;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario.util.MarioCPMCompat;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.RandomSeed;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MarioServerPlayerData extends MarioMoveableData implements IMarioAuthoritativeData {
	private final ServerPlayerEntity MARIO;
	public MarioServerPlayerData(ServerPlayerEntity mario) {
		super();
		this.MARIO = mario;
	}
	@Override public ServerPlayerEntity getMario() {
		return this.MARIO;
	}

	private final Set<Pair<AbstractParsedAction, Long>> RECENT_ACTIONS = new HashSet<>();

	public void preInitialApply(boolean enabled, ParsedPowerUp powerUp, ParsedCharacter character) {
		this.loadFromNbtBeforeNetworkHandler(enabled, powerUp, character);
	}

	@Override
	public void initialApply() {
		super.initialApply();
//		this.syncToClient(this.getMario());
	}

	@Override
	public void setEnabledInternal(boolean enabled) {
		super.setEnabledInternal(enabled);
		if(enabled) this.updatePlayerModel();
		else MarioCPMCompat.getCommonAPI().resetPlayerModel(PlayerEntity.class, this.getMario());
	}

	@Override
	public boolean setAction(@Nullable AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, boolean forced, boolean fromCommand) {
		if(!forced && !fromCommand) {
			if(!this.getAction().equals(fromAction)) {
				// Check if we were recently in fromAction. If not, return false.
				if(fromAction == null || this.RECENT_ACTIONS.stream().noneMatch(pair -> pair.getLeft().ID.equals(fromAction.ID))) {
					if (MarioQuaMario.LOGGER.isWarnEnabled()) {
						Identifier fromActionID = fromAction == null ? null : fromAction.ID;
						StringBuilder recentActionsString = new StringBuilder();
						for (Pair<AbstractParsedAction, Long> recentAction : RECENT_ACTIONS) {
							recentActionsString.append("\n").append(recentAction.getLeft().ID);
						}
						MarioQuaMario.LOGGER.warn(
								"TRANSITION REJECTED: Not recently in fromAction.\nServer-sided action: {}\nAttempted {} -> {}\nRecent actions: {}",
								this.getActionID(), fromActionID, toAction.ID, recentActionsString);
					}
					return false;
				}
			}

			@Nullable ParsedTransition transition = fromAction.TRANSITIONS_FROM_TARGETS.get(toAction);
			if(transition != null && transition.serverChecked() && !transition.evaluator().shouldTransition(this)) {
				MarioQuaMario.LOGGER.warn("TRANSITION REJECTED: Transition is server-checked and evaluator failed.\nAttempted {} -> {}",
						fromAction.ID, toAction.ID);
				return false;
			}
		}

		return super.setAction(fromAction, toAction, seed, forced, fromCommand);
	}

	@Override
	public void setActionTransitionless(AbstractParsedAction action) {
		this.RECENT_ACTIONS.add(new Pair<>(this.getAction(), this.getMario().getWorld().getTime() + 10L));
		super.setActionTransitionless(action);
	}

	@Override
	public boolean setPowerUpTransitionless(ParsedPowerUp newPowerUp) {
		if(!this.updatePlayerModel(newPowerUp)) return false;
		return super.setPowerUpTransitionless(newPowerUp);
	}

	private boolean updatePlayerModel(ParsedPowerUp newPowerUp) {
		String newModel = this.getCharacter().MODELS.get(newPowerUp);
		MarioQuaMario.LOGGER.info("Changing to model for combination ({}, {})", this.getCharacterID(), newPowerUp.ID);
		if(newModel == null) return false;
		if(this.getMario().networkHandler != null)
			MarioCPMCompat.getCommonAPI().setPlayerModel(PlayerEntity.class, this.getMario(), newModel, true, false);
		return true;
	}
	public void updatePlayerModel() {
		this.updatePlayerModel(this.getPowerUp());
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.getAction().serverTick(this);
		this.getPowerUp().serverTick(this);
		this.getCharacter().serverTick(this);

		long worldTime = this.getMario().getWorld().getTime();
		this.RECENT_ACTIONS.removeIf(pair -> worldTime > pair.getRight());
	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);

		boolean cancelVanillaTravel = this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();
		this.getMario().move(MovementType.SELF, this.getMario().getVelocity());

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);

		this.getMario().updateLimbs(false);
		return cancelVanillaTravel;
	}

	@Override public MarioInputs getInputs() {
		return PHONY_INPUTS;
	}

	public static final MarioInputs PHONY_INPUTS;
	static {
		MarioInputs.MarioButton phonyButton = new MarioInputs.MarioButton() {
			@Override public boolean isPressed() {
				return false;
			}
			@Override public boolean isHeld() {
				return false;
			}
		};
		PHONY_INPUTS = new MarioInputs(phonyButton, phonyButton, phonyButton) {
			@Override public double getForwardInput() {
				return 0;
			}
			@Override public double getStrafeInput() {
				return 0;
			}
			@Override public boolean isReal() {
				return false;
			}
		};
	}

	public void syncToClient(ServerPlayerEntity toWhom) {
		this.setEnabled(this.isEnabled());
		MarioDataPackets.assignCharacterS2C(this.getMario(), this.getCharacter());
		MarioDataPackets.assignPowerUpS2C(this.getMario(), this.getPowerUp());
		MarioDataPackets.assignActionS2C(this.getMario(), true, this.getAction());
//		MarioDataPackets.updatePlayermodelS2C(this.getMario());
	}

	// CUTOFF FOR IMarioAuthoritativeData IMPLEMENTATION:---------------------------------------------------------------
	@Override public void setEnabled(boolean enable) {

	}

	@Override public boolean transitionToAction(Identifier actionID) {
		long seed = RandomSeed.getSeed();
		AbstractParsedAction toAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action (" + actionID + ") doesn't exist!");
		if(this.setAction(this.getAction(), toAction, seed, false, false)) {
			MarioDataPackets.transitionToActionS2C(this.getMario(), true, this.getAction(), toAction, seed);
		}
		return false;
	}
	@Override public boolean transitionToAction(String actionID) {
		return this.transitionToAction(Identifier.of(actionID));
	}

	@Override public void assignAction(Identifier actionID) {
		AbstractParsedAction newAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action (" + actionID + ") doesn't exist!");
		this.setActionTransitionless(newAction);
		MarioDataPackets.assignActionS2C(this.getMario(), true, newAction);
	}
	@Override public void assignAction(String actionID) {
		this.assignAction(Identifier.of(actionID));
	}

	@Override public boolean empowerTo(Identifier powerUpID) {
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		if(!this.setPowerUp(newPowerUp, false, seed)) return false;
		MarioDataPackets.empowerRevertS2C(this.getMario(), newPowerUp, false, seed);
		return true;
	}
	@Override public boolean empowerTo(String powerUpID) {
		return this.empowerTo(Identifier.of(powerUpID));
	}

	@Override public boolean revertTo(Identifier powerUpID) {
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		if(!this.setPowerUp(newPowerUp, true, seed)) return false;
		MarioDataPackets.empowerRevertS2C(this.getMario(), newPowerUp, true, seed);
		return true;
	}
	@Override public boolean revertTo(String powerUpID) {
		return this.revertTo(Identifier.of(powerUpID));
	}

	@Override public boolean assignPowerUp(Identifier powerUpID) {
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		if(!this.setPowerUpTransitionless(newPowerUp)) return false;
		MarioDataPackets.assignPowerUpS2C(this.getMario(), newPowerUp);
		return true;
	}
	@Override public boolean assignPowerUp(String powerUpID) {
		return this.assignPowerUp(Identifier.of(powerUpID));
	}

	@Override public void assignCharacter(Identifier characterID) {
		ParsedCharacter newCharacter = Objects.requireNonNull(RegistryManager.CHARACTERS.get(characterID),
				"Target character (" + characterID + ") doesn't exist!");

		this.setCharacter(newCharacter);
		MarioDataPackets.assignCharacterS2C(this.getMario(), newCharacter);
	}
	@Override public void assignCharacter(String characterID) {
		this.assignCharacter(Identifier.of(characterID));
	}
}
