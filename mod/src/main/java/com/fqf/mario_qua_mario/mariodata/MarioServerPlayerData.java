package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.ParsedTransition;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import com.fqf.mario_qua_mario.registries.actions.parsed.ParsedWallboundAction;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario.compat.MarioCPMCompat;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.tom.cpm.shared.io.ModelFile;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.RandomSeed;
import org.jetbrains.annotations.NotNull;
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

	public boolean cancelNextRequestTeleportPacket;

	private final Set<Pair<AbstractParsedAction, Long>> RECENT_ACTIONS = new HashSet<>();

	@Override public void initialApply() {
		if(this.isEnabled()) {
			ParsedPowerUp preApplyPowerUp = this.getPowerUp();
			this.setCharacter(this.getCharacter());
			this.setPowerUpTransitionless(preApplyPowerUp);
			MarioDataPackets.syncMarioDataToPlayerS2C(this.getMario(), this.getMario());
		}
		else super.initialApply();
//		this.syncToClient(this.getMario());
	}

	@Override
	public void updatePassiveUniversalTraits(boolean enabled) {
		super.updatePassiveUniversalTraits(enabled);
		if(enabled) this.updatePlayerModel();
		else MarioCPMCompat.getCommonAPI().resetPlayerModel(PlayerEntity.class, this.getMario());
	}

	@Override
	public boolean setAction(@Nullable AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, boolean forced, boolean fromCommand) {
		if(!forced && !fromCommand) {
			if(!this.getAction().equals(fromAction)) {
				if(fromAction == null) {
					MarioQuaMario.LOGGER.warn("TRANSITION REJECTED: fromAction is null. Trying to transition from null to {}",
							toAction.ID);
					return false;
				}

				// Check if we were recently in fromAction. If not, return false.
				if(!this.recentlyInAction(fromAction)) {
					if (MarioQuaMario.LOGGER.isWarnEnabled()) { // is there a reason to bother checking this????
						StringBuilder recentActionsString = new StringBuilder();
						for (Pair<AbstractParsedAction, Long> recentAction : RECENT_ACTIONS) {
							recentActionsString.append("\n").append(recentAction.getLeft().ID);
						}
						MarioQuaMario.LOGGER.warn("""
								TRANSITION REJECTED: Not recently in fromAction.
								Server-sided action: {}
								Attempted {} -> {}
								Recent actions: {}""", this.getActionID(), fromAction.ID, toAction.ID, recentActionsString);
					}
					return false;
				}
			}

			// Check if our current action is a Mounted action and the fromAction isn't. If so, return false.
			if(this.getActionCategory() == ActionCategory.MOUNTED && fromAction.CATEGORY != ActionCategory.MOUNTED) {
				MarioQuaMario.LOGGER.warn("""
							TRANSITION REJECTED: Trying to execute non-mounted transition while in mounted action.
							Server-sided action: {}
							Attempted: {} -> {}""", this.getActionID(), fromAction.ID, toAction.ID);
				return false;
			}

			// Check if we're trying to transition into a Wallbound action.
			if(toAction.CATEGORY == ActionCategory.WALLBOUND) {
				ParsedWallboundAction wallAction = (ParsedWallboundAction) toAction;

				// If last received wall yaw packet was too long ago, reject
				if(this.getMario().getWorld().getTime() > this.lastReceivedWallYawTime + 60L) {
					MarioQuaMario.LOGGER.warn("""
							TRANSITION REJECTED: Trying to enter Wallbound Action, but last wall yaw packet was\
							 received too long ago.
							Server-sided action: {}
							Attempted: {} -> {}
							Last wall yaw given was {}
							Last wall yaw packet received at {}""",
							this.getActionID(), fromAction.ID, toAction.ID, this.lastReceivedWallYaw, this.lastReceivedWallYawTime);
					return false;
				}

				// Else, assign the new yaw:
				this.getWallInfo().setYaw(this.lastReceivedWallYaw);
				// And if legality check fails, REJECT
				if(!wallAction.verifyWallLegality(this)) {
					MarioQuaMario.LOGGER.warn("""
							TRANSITION REJECTED: Trying to enter Wallbound Action, but legality check failed.
							Server-sided action: {}
							Attempted: {} -> {}""", this.getActionID(), fromAction.ID, toAction.ID);
					return false;
				}

				// Else, broadcast wall yaw to clients:

			}

			@Nullable ParsedTransition transition = fromAction.TRANSITIONS_FROM_TARGETS.get(toAction);
			if(transition != null && transition.serverChecked() && !transition.evaluator().shouldTransition(this)) {
				MarioQuaMario.LOGGER.warn("""
						TRANSITION REJECTED: Transition is server-checked and evaluator failed.
						Attempted {} -> {}""", fromAction.ID, toAction.ID);
				return false;
			}
		}

		return super.setAction(fromAction, toAction, seed, forced, fromCommand);
	}

	public boolean recentlyInAction(AbstractParsedAction checkAction) {
		return this.getAction() == checkAction || this.RECENT_ACTIONS.stream().anyMatch(pair -> pair.getLeft().ID.equals(checkAction.ID));
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
		ModelFile newModel = this.getCharacter().MODELS.get(newPowerUp);
		if(newModel == null) {
			MarioQuaMario.LOGGER.error("Attempting to set {}'s power-up, however there is no model for combination {} + {}!",
					this.getMario().getName().getString(), this.getCharacterID(), newPowerUp.ID);
			return false;
		}
		if(this.getMario().networkHandler != null)
			MarioCPMCompat.getCommonAPI().setPlayerModel(PlayerEntity.class, this.getMario(), newModel, true);
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

	private float lastReceivedWallYaw = Float.NaN;
	private long lastReceivedWallYawTime = Long.MIN_VALUE;
	public void receiveWallYaw(float wallYaw) {
		this.lastReceivedWallYaw = wallYaw;
		this.lastReceivedWallYawTime = this.getMario().getWorld().getTime();
	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);

		boolean cancelVanillaTravel = this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();
		this.getMario().move(MovementType.SELF, this.getMovementWithFluidPushing());

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

//	public void syncToClient(ServerPlayerEntity toWhom) {
//		this.setEnabled(this.isEnabled());
//		MarioDataPackets.assignCharacterS2C(this.getMario(), this.getCharacter());
//		MarioDataPackets.assignPowerUpS2C(this.getMario(), this.getPowerUp());
//		MarioDataPackets.assignActionS2C(this.getMario(), true, this.getAction());
////		MarioDataPackets.updatePlayermodelS2C(this.getMario());
//	}

	// CUTOFF FOR IMarioAuthoritativeData IMPLEMENTATION:---------------------------------------------------------------
	@Override public void disable() {
		this.disableInternal();
		MarioDataPackets.disableMarioS2C(this.getMario());
	}

	@Override public ActionTransitionResult forceActionTransition(@Nullable Identifier fromID, @NotNull Identifier toID) {
		if(!this.isEnabled()) return ActionTransitionResult.NOT_ENABLED;
		long seed = RandomSeed.getSeed();

		AbstractParsedAction fromAction;
		if(fromID == null) fromAction = this.getAction();
		else fromAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(fromID),
				"Pre-transition action (" + toID + ") doesn't exist!");

		AbstractParsedAction toAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(toID),
				"Target action (" + toID + ") doesn't exist!");

		if(this.setAction(fromAction, toAction, seed, false, true)) {
			MarioDataPackets.transitionToActionS2C(this.getMario(), true, fromAction, toAction, seed);
			return ActionTransitionResult.SUCCESS;
		}
		return ActionTransitionResult.NO_SUCH_TRANSITION;
	}
	@Override public ActionTransitionResult forceActionTransition(@Nullable String fromID, @NotNull String toID) {
		return this.forceActionTransition(fromID == null ? null : Identifier.of(fromID), Identifier.of(toID));
	}

	@Override public ActionChangeOperationResult assignAction(Identifier actionID) {
		if(!this.isEnabled()) return ActionChangeOperationResult.NOT_ENABLED;
		AbstractParsedAction newAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action (" + actionID + ") doesn't exist!");
		this.setActionTransitionless(newAction);
		MarioDataPackets.assignActionS2C(this.getMario(), true, newAction);
		return ActionChangeOperationResult.SUCCESS;
	}
	@Override public ActionChangeOperationResult assignAction(String actionID) {
		return this.assignAction(Identifier.of(actionID));
	}

	@Override public PowerChangeOperationResult empowerTo(Identifier powerUpID) {
		if(!this.isEnabled()) return PowerChangeOperationResult.NOT_ENABLED;
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		if(!this.setPowerUp(newPowerUp, false, seed)) return PowerChangeOperationResult.MISSING_PLAYERMODEL;
		MarioDataPackets.empowerRevertS2C(this.getMario(), newPowerUp, false, seed);
		return PowerChangeOperationResult.SUCCESS;
	}
	@Override public PowerChangeOperationResult empowerTo(String powerUpID) {
		return this.empowerTo(Identifier.of(powerUpID));
	}

	@Override
	public ReversionResult executeReversion() {
		if(!this.isEnabled()) return ReversionResult.NOT_ENABLED;

		ServerPlayerEntity mario = this.getMario();
		Identifier reversionTarget = this.getPowerUp().REVERSION_TARGET_ID;
		if(reversionTarget == null) return ReversionResult.NO_WEAKER_FORM;

		if(mario.getWorld().getGameRules().getBoolean(MarioGamerules.REVERT_TO_SMALL)) {
			while(Objects.requireNonNull(RegistryManager.POWER_UPS.get(reversionTarget)).REVERSION_TARGET_ID != null) {
				reversionTarget = Objects.requireNonNull(RegistryManager.POWER_UPS.get(reversionTarget)).REVERSION_TARGET_ID;
			}
		}
		if(this.revertTo(reversionTarget) == PowerChangeOperationResult.MISSING_PLAYERMODEL) {
			MarioQuaMario.LOGGER.warn(
					"{}'s current power up ({}) should revert to {}, however this is illegal for their character! ({})",
					mario.getName().getString(), this.getPowerUpID(), reversionTarget, this.getCharacterID()
			);
			return ReversionResult.MISSING_PLAYERMODEL;
		}
		mario.setHealth(mario.getMaxHealth());
		return ReversionResult.SUCCESS;
	}

	@Override public PowerChangeOperationResult revertTo(Identifier powerUpID) {
		if(!this.isEnabled()) return PowerChangeOperationResult.NOT_ENABLED;
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		if(!this.setPowerUp(newPowerUp, true, seed)) return PowerChangeOperationResult.MISSING_PLAYERMODEL;
		MarioDataPackets.empowerRevertS2C(this.getMario(), newPowerUp, true, seed);
		return PowerChangeOperationResult.SUCCESS;
	}
	@Override public PowerChangeOperationResult revertTo(String powerUpID) {
		return this.revertTo(Identifier.of(powerUpID));
	}

	@Override public PowerChangeOperationResult assignPowerUp(Identifier powerUpID) {
		if(!this.isEnabled()) return PowerChangeOperationResult.NOT_ENABLED;
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		if(!this.setPowerUpTransitionless(newPowerUp)) return PowerChangeOperationResult.MISSING_PLAYERMODEL;
		MarioDataPackets.assignPowerUpS2C(this.getMario(), newPowerUp);
		return PowerChangeOperationResult.SUCCESS;
	}
	@Override public PowerChangeOperationResult assignPowerUp(String powerUpID) {
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
