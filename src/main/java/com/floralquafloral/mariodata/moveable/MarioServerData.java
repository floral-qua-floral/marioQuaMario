package com.floralquafloral.mariodata.moveable;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class MarioServerData extends MarioMoveableData {
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
		return !marioServer.hasVehicle();
	}

	@Override
	public void setActionTransitionless(ParsedAction action) {
		MarioQuaMario.LOGGER.info("MarioServerData setAction to {}", action.ID);

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
