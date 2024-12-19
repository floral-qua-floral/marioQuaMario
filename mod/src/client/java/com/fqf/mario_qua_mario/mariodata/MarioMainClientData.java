package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;

public class MarioMainClientData extends MarioMoveableData implements IMarioClientDataImpl {
	private ClientPlayerEntity mario;
	public MarioMainClientData() {
		super();
	}
	@Override public ClientPlayerEntity getMario() {
		return mario;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.mario = (ClientPlayerEntity) mario;
		super.setMario(mario);
	}

	@Override
	public void tick() {
		super.tick();
		this.INPUTS.updateButtons();
		this.getAction().clientTick(this, true);
		this.getPowerUp().clientTick(this, true);
		this.getCharacter().clientTick(this, true);
	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
		this.INPUTS.updateAnalog(forwardInput, strafeInput);

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

	private final RealInputs INPUTS = new RealInputs();
	@Override public MarioInputs getInputs() {
		return this.INPUTS;
	}

	private class RealInputs extends MarioInputs {
		private final ClientButton JUMP_CLIENT;
		private final ClientButton DUCK_CLIENT;
		private final ClientButton SPIN_CLIENT;

		private final ClientButton LEFT;
		private final ClientButton RIGHT;

		public RealInputs() {
			super(new ClientButton(), new ClientButton(), new ClientButton());
			this.JUMP_CLIENT = (ClientButton) this.JUMP;
			this.DUCK_CLIENT = (ClientButton) this.DUCK;
			this.SPIN_CLIENT = (ClientButton) this.SPIN;

			this.LEFT = new ClientButton();
			this.RIGHT = new ClientButton();
		}

		private static class ClientButton implements MarioButton {
			private boolean isHeld = false;
			private int pressBuffer = 0;

			@Override public boolean isHeld() {
				return isHeld;
			}
			@Override public boolean isPressed() {
				return this.isPressedNoUnbuffer() && this.unbuffer();
			}
			public boolean isPressedNoUnbuffer() {
				return this.pressBuffer > 0;
			}

			private boolean unbuffer() {
				this.pressBuffer = 0;
				return true;
			}

			private void update(boolean isHeld) {
				this.update(isHeld, isHeld);
			}
			private void update(boolean isHeld, boolean isPressed) {
				if(isHeld && isPressed && !this.isHeld)
					this.pressBuffer = 3;
				else
					this.pressBuffer--;
				this.isHeld = isHeld;
			}
		}

		@Override public boolean isReal() {
			return true;
		}
		double forwardInput;
		@Override public double getForwardInput() {
			return this.forwardInput;
		}
		double strafeInput;
		@Override public double getStrafeInput() {
			return this.strafeInput;
		}

		private void updateButtons() {
			ClientPlayerEntity mario = getMario();
			Input inputs = mario.input;

			this.LEFT.update(inputs.pressingLeft);
			this.RIGHT.update(inputs.pressingRight);

			this.JUMP_CLIENT.update(inputs.jumping);
			this.DUCK_CLIENT.update(inputs.sneaking || mario.isInSneakingPose(), inputs.sneaking);
			this.SPIN_CLIENT.update(LEFT.isHeld && RIGHT.isHeld,
					LEFT.isPressedNoUnbuffer() && RIGHT.isPressedNoUnbuffer());
		}
		private void updateAnalog(double forwardInput, double strafeInput) {
			this.forwardInput = forwardInput;
			this.strafeInput = strafeInput;
		}
	}
}
