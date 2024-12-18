package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSeed;

import java.util.Objects;

public class MarioServerPlayerData extends MarioMoveableData implements IMarioAuthoritativeData {
	private ServerPlayerEntity mario;
	public MarioServerPlayerData(ServerPlayerEntity mario) {
		super();
		this.mario = mario;
	}
	@Override public ServerPlayerEntity getMario() {
		return this.mario;
	}

	@Override public void setEnabled(boolean enable) {

	}

	@Override public boolean transitionToAction(Identifier actionID) {
		long seed = RandomSeed.getSeed();
		AbstractParsedAction toAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action doesn't exist!");
		if(this.setAction(this.getAction(), toAction, seed, false)) {
			MarioDataPackets.setActionS2C(this.getMario(), true, this.getAction(), toAction, seed);
		}
		return false;
	}
	@Override public boolean transitionToAction(String actionID) {
		return this.transitionToAction(Identifier.of(actionID));
	}

	@Override public void assignAction(Identifier actionID) {
		AbstractParsedAction newAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action doesn't exist!");
		this.setActionTransitionless(newAction);
		MarioDataPackets.setActionTransitionlessS2C(this.getMario(), true, newAction);
	}
	@Override public void assignAction(String actionID) {
		this.assignAction(Identifier.of(actionID));
	}

	@Override public void empowerTo(Identifier powerUpID) {

	}
	@Override public void empowerTo(String powerUpID) {
		this.empowerTo(Identifier.of(powerUpID));
	}

	@Override public void revertTo(Identifier powerUpID) {

	}
	@Override public void revertTo(String powerUpID) {
		this.revertTo(Identifier.of(powerUpID));
	}

	@Override public void assignPowerUp(Identifier powerUpID) {

	}
	@Override public void assignPowerUp(String powerUpID) {
		this.assignPowerUp(Identifier.of(powerUpID));
	}

	@Override public void assignCharacter(Identifier characterID) {

	}
	@Override public void assignCharacter(String characterID) {
		this.assignCharacter(Identifier.of(characterID));
	}

	@Override public void setMario(PlayerEntity mario) {
		this.mario = (ServerPlayerEntity) mario;
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
	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);

		this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();
		this.getMario().move(MovementType.SELF, this.getMario().getVelocity());

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);

		this.getMario().updateLimbs(false);
		return true;
	}

	@Override public MarioInputs getInputs() {
		return PHONY_INPUTS;
	}

	private static final MarioInputs PHONY_INPUTS;
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
}
