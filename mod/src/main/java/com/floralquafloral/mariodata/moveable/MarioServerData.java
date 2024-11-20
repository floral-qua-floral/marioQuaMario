package com.floralquafloral.mariodata.moveable;

import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioDataPackets;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MarioServerData extends MarioMoveableData implements MarioAuthoritativeData {
	private ServerPlayerEntity marioServer;
	public MarioServerData(ServerPlayerEntity mario) {
		super(mario);
		this.marioServer = mario;
	}

	@Override public ServerPlayerEntity getMario() {
		return this.marioServer;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.marioServer = (ServerPlayerEntity) mario;
		super.setMario(mario);
	}

	@Override public boolean travelHook(double forwardInput, double strafeInput) {
		getAction().travelHook(this);
		applyModifiedVelocity();
		marioServer.move(MovementType.SELF, marioServer.getVelocity()); // ???????
		return !marioServer.hasVehicle();
	}

	@Override
	public void setActionTransitionless(ParsedAction action) {
		if(this.getAction().ANIMATION != null)
			CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.marioServer, this.getAction().ANIMATION, 0);
		if(action.ANIMATION != null)
			CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.marioServer, action.ANIMATION, 1);

		super.setActionTransitionless(action);
	}

	@Override public void tick() {
		this.getAction().serverTick(this);
		this.getPowerUp().serverTick(this);
		this.getCharacter().serverTick(this);
	}

	private static final PhonyInputs PHONY_INPUTS = new PhonyInputs();
	@Override public @NotNull MarioInputs getInputs() {
		return PHONY_INPUTS;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		MarioDataPackets.setMarioEnabled(marioServer, isEnabled);
	}

	@Override
	public void setCharacter(Identifier id) {
		MarioDataPackets.setMarioCharacter(marioServer, Objects.requireNonNull(RegistryManager.CHARACTERS.get(id)));
	}

	@Override
	public void setCharacter(String id) {
		this.setCharacter(Identifier.of(id));
	}

	@Override
	public void setPowerUp(Identifier id) {
		MarioDataPackets.setMarioPowerUp(marioServer, Objects.requireNonNull(RegistryManager.POWER_UPS.get(id)));
	}

	@Override
	public void setPowerUp(String id) {
		this.setPowerUp(Identifier.of(id));
	}

	@Override
	public boolean setAction(Identifier id) {
		return MarioDataPackets.setMarioAction(marioServer, Objects.requireNonNull(RegistryManager.ACTIONS.get(id)), Random.create().nextLong(), true);
	}

	@Override
	public boolean setAction(String id) {
		return this.setAction(Identifier.of(id));
	}

	@Override
	public void setActionTransitionless(Identifier id) {
		MarioDataPackets.forceSetMarioAction(marioServer, Objects.requireNonNull(RegistryManager.ACTIONS.get(id)));
	}

	@Override
	public void setActionTransitionless(String id) {
		this.setActionTransitionless(Identifier.of(id));
	}

	private static class PhonyInputs extends MarioInputs {
		private static class PhonyButton implements ButtonInput {
			@Override public boolean isHeld() {
				return false;
			}
			@Override public boolean isPressed() {
				return false;
			}
			@Override public boolean isPressedNoUnbuffer() {
				return false;
			}
		}

		private static final PhonyButton PHONY_BUTTON = new PhonyButton();
		private PhonyInputs() {
			super(PHONY_BUTTON, PHONY_BUTTON, PHONY_BUTTON);
		}

		@Override public boolean isReal() {
			return false;
		}
		@Override public double getForwardInput() {
			return 0;
		}
		@Override public double getStrafeInput() {
			return 0;
		}
	}
}
