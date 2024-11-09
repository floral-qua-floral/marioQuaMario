package com.floralquafloral.mariodata.moveable;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.action.TransitionPhase;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class MarioMainClientData extends MarioMoveableData implements MarioClientSideData {
	private static MarioMainClientData instance;
	public static MarioMainClientData getInstance() {
		return instance;
	}

	private ClientPlayerEntity marioClient;
	private final ClientInputs INPUTS;

	@Override public ClientPlayerEntity getMario() {
		return marioClient;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.marioClient = (ClientPlayerEntity) mario;
		super.setMario(mario);
	}

	public MarioMainClientData(ClientPlayerEntity mario) {
		super(mario);
		this.marioClient = mario;
		this.INPUTS = new ClientInputs();
		MarioMainClientData.instance = this;
	}

	@Override public void setAction(ParsedAction action, long seed) {
		this.setActionTransitionless(action);
	}

	@Override public void setActionTransitionless(ParsedAction action) {
		MarioQuaMario.LOGGER.info("MarioMainClientData setAction to " + action.ID);
		if(action != getAction()) getTimers().actionTimer = 0;
		if(this.getAction().ANIMATION != null)
			CPMIntegration.clientAPI.playAnimation(getAction().ANIMATION, 0);
		if(action.ANIMATION != null)
			CPMIntegration.clientAPI.playAnimation(action.ANIMATION, 1);
		super.setActionTransitionless(action);
	}

	@Override public void tick() {
		this.INPUTS.updateButtons(this);

		this.getAction().clientTick(this, true);
		this.getPowerUp().clientTick(this, true);
		this.getCharacter().clientTick(this, true);
	}

	@Override public boolean travelHook(double forwardInput, double strafeInput) {
		this.INPUTS.updateAnalog(forwardInput, strafeInput);

		// TODO: Levitation effect functionality

		getAction().attemptTransitions(this, TransitionPhase.PRE_TICK);
		getAction().travelHook(this);
//		getMario().strideDistance = 0;
//		getMario().prevStrideDistance = 0;
		getAction().attemptTransitions(this, TransitionPhase.POST_TICK);

		applyModifiedVelocity();
		marioClient.move(MovementType.SELF, marioClient.getVelocity());
		if(getAction().attemptTransitions(this, TransitionPhase.POST_MOVE))
			applyModifiedVelocity();

		marioClient.updateLimbs(false);
		return true;
	}

	@Override public @NotNull MarioInputs getInputs() {
		return this.INPUTS;
	}
	private static class ClientInputs extends MarioInputs {
		private final ClientButton JUMP_CLIENT;
		private final ClientButton DUCK_CLIENT;
		private final ClientButton SPIN_CLIENT;

		private final ClientButton LEFT;
		private final ClientButton RIGHT;

		public ClientInputs() {
			super(new ClientButton(), new ClientButton(), new ClientButton());
			this.JUMP_CLIENT = (ClientButton) this.JUMP;
			this.DUCK_CLIENT = (ClientButton) this.DUCK;
			this.SPIN_CLIENT = (ClientButton) this.SPIN;

			this.LEFT = new ClientButton();
			this.RIGHT = new ClientButton();
		}

		private static class ClientButton implements ButtonInput {
			private boolean isHeld = false;
			private int pressBuffer = 0;

			@Override public boolean isHeld() {
				return isHeld;
			}
			@Override public boolean isPressed() {
				return this.isPressedNoUnbuffer() && this.unbuffer();
			}
			@Override public boolean isPressedNoUnbuffer() {
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
					this.pressBuffer = MarioQuaMario.CONFIG.getBufferLength();
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

		private void updateButtons(MarioMainClientData data) {
			ClientPlayerEntity mario = data.getMario();

			this.LEFT.update(mario.input.pressingLeft);
			this.RIGHT.update(mario.input.pressingRight);

			this.JUMP_CLIENT.update(mario.input.jumping);
			this.DUCK_CLIENT.update(mario.input.sneaking || mario.isInSneakingPose(), mario.input.sneaking);
			this.SPIN_CLIENT.update(LEFT.isHeld && RIGHT.isHeld,
					LEFT.isPressedNoUnbuffer() && RIGHT.isPressedNoUnbuffer());
		}
		private void updateAnalog(double forwardInput, double strafeInput) {
			this.forwardInput = forwardInput;
			this.strafeInput = strafeInput;
		}
	}

}
