package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;

public class MarioMainClientData extends MarioMoveableData implements IMarioClientDataImpl {
	private ClientPlayerEntity mario;
	public MarioMainClientData(ClientPlayerEntity mario) {
		super();
		this.mario = mario;
	}
	@Override public ClientPlayerEntity getMario() {
		return mario;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.mario = (ClientPlayerEntity) mario;
	}

	@Override
	public void tick() {
		this.INPUTS.updateButtons();
	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
		this.INPUTS.updateAnalog(forwardInput, strafeInput);

		this.getAction().travelHook(this);


		this.applyModifiedVelocity();
		this.getMario().move(MovementType.SELF, this.getMario().getVelocity());

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
