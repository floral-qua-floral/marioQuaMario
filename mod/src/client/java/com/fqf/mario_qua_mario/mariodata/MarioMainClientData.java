package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MarioMainClientData extends MarioMoveableData implements IMarioClientDataImpl {
	private final ClientPlayerEntity MARIO;
	public MarioMainClientData(ClientPlayerEntity mario) {
		super();
		this.MARIO = mario;
	}
	@Override public ClientPlayerEntity getMario() {
		return MARIO;
	}

	@Override public boolean setPowerUp(ParsedPowerUp newPowerUp, boolean isReversion, long seed) {
		this.handlePowerTransitionSound(isReversion, newPowerUp, seed);
		return super.setPowerUp(newPowerUp, isReversion, seed);
	}

	private CameraAnimation currentCameraAnimation;
	private float cameraAnimationTime;
	private boolean attemptFinish;

	@Override public void setActionTransitionless(AbstractParsedAction action) {
		this.handleSlidingSound(action);
		this.MARIO.mqm$getAnimationData().replaceAnimation(this, action.ANIMATION, -1);

		if(action != this.getAction()) {
			CameraAnimation newAnim = action.CAMERA_ANIMATIONS == null ? null : switch (action.CAMERA_ANIMATIONS.optionGetter().get()) {
				case AUTHENTIC -> action.CAMERA_ANIMATIONS.authentic();
				case GENTLE -> {
					CameraAnimation gentleAnimation = action.CAMERA_ANIMATIONS.gentle();
					yield gentleAnimation == null ? action.CAMERA_ANIMATIONS.authentic() : gentleAnimation;
				}
				case MINIMAL -> action.CAMERA_ANIMATIONS.minimal();
			};

			if(newAnim != null) {
				this.currentCameraAnimation = newAnim;
				this.cameraAnimationTime = 0;
				this.attemptFinish = false;
			}
			else this.attemptFinish = true;
		}

		super.setActionTransitionless(action);
	}

	public boolean animatingCamera() {
		return this.isEnabled() && this.currentCameraAnimation != null && !this.getMario().isSleeping();
	}

	public void mutateCamera(Arrangement cameraArrangement, float tickDelta) {
		this.cameraAnimationTime += MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
		float progress = this.currentCameraAnimation.progressHandler().progressCalculator().calculateProgress(this, this.cameraAnimationTime);

		if(this.attemptFinish && progress >= this.currentCameraAnimation.progressHandler().minProgressToFinish()) {
			this.currentCameraAnimation = null;
			this.attemptFinish = false;
		}
		else this.getMario().mqm$getAnimationData().mutate(cameraArrangement, this.currentCameraAnimation.mutator(), this, progress);
	}

	@Override public void tick() {
		super.tick();
		this.getAction().clientTick(this, true);
		this.getPowerUp().clientTick(this, true);
		this.getCharacter().clientTick(this, true);
		if(!MinecraftClient.getInstance().options.getPerspective().isFirstPerson())
			this.currentCameraAnimation = null;
	}

	public void tickInputs() {
		this.INPUTS.updateButtons();
	}

	@Override public boolean travelHook(double forwardInput, double strafeInput) {
		this.INPUTS.updateAnalog(forwardInput, strafeInput);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);

		boolean cancelVanillaTravel = this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();
		this.moveWithFluidPushing();
//		this.getMario().move(MovementType.SELF, this.getMario().getVelocity());

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		this.applyModifiedVelocity();

		this.getMario().updateLimbs(false);
		return cancelVanillaTravel;
	}

	private final RealInputs INPUTS = new RealInputs();
	@Override public MarioInputs getInputs() {
		return this.INPUTS;
	}

	private final Map<Identifier, SoundInstance> STORED_SOUNDS = new HashMap<>();
	@Override public Map<Identifier, SoundInstance> getStoredSounds() {
		return this.STORED_SOUNDS;
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
