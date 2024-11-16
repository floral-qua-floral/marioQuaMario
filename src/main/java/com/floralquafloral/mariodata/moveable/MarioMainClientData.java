package com.floralquafloral.mariodata.moveable;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.bumping.BumpManager;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mixin.CameraMixin;
import com.floralquafloral.registries.states.action.ActionDefinition;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.action.TransitionPhase;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockCollisionSpliterator;
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

	public ActionDefinition.CameraAnimation cameraAnimation;
	public int cameraAnimationLoops;
	public int TEMPORARY;
	public float TEMPORARY2;
	public boolean cameraAnimationDoneLooping;
	public float cameraAnimationStartTime;
	public float cameraAnimationProgression;
	public final float[] CAMERA_ROTATIONS = new float[3];

	@Override public void setActionTransitionless(ParsedAction action) {
//		MarioQuaMario.LOGGER.info("MarioMainClientData setAction to " + action.ID);
		if(action != getAction()) getTimers().actionTimer = 0;

		// CPM animation
		if(this.getAction().ANIMATION != null)
			CPMIntegration.clientAPI.playAnimation(getAction().ANIMATION, 0);
		if(action.ANIMATION != null)
			CPMIntegration.clientAPI.playAnimation(action.ANIMATION, 1);

		// Camera animation
		if(action != this.getAction()) {
			ActionDefinition.CameraAnimation newCameraAnimation = action.getCameraAnimation();
			if (newCameraAnimation != null) {
				this.cameraAnimation = newCameraAnimation;
				this.cameraAnimationLoops = 1;
				this.cameraAnimationDoneLooping = !newCameraAnimation.SHOULD_LOOP;
				this.cameraAnimationStartTime = marioClient.getWorld().getTime() + 1;
				this.cameraAnimationProgression = 0;
				TEMPORARY = 0;
				TEMPORARY2 = 0;

				this.CAMERA_ROTATIONS[0] = 0.0F;
				this.CAMERA_ROTATIONS[1] = 0.0F;
				this.CAMERA_ROTATIONS[2] = 0.0F;
			} else this.cameraAnimationDoneLooping = true;
		}

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
		getAction().attemptTransitions(this, TransitionPhase.POST_TICK);

		getTimers().jumpLandingTime--;
		getTimers().doubleJumpLandingTime--;

		applyModifiedVelocity();
		marioClient.move(MovementType.SELF, marioClient.getVelocity());

		ActionDefinition.BumpingRule bumpingRule = getAction().BUMPING_RULE;
		if(bumpingRule != null) {
			if (marioClient.verticalCollision) {
				if (marioClient.groundCollision) {
					if (bumpingRule.FLOORS > 0) BumpManager.bumpBlocks(
							this,
							marioClient.clientWorld,
							getBumpPositions(0, -0.15, 0),
							Direction.DOWN,
							bumpingRule.FLOORS
					);
				} else if (bumpingRule.CEILINGS > 0) BumpManager.bumpBlocks(
						this,
						marioClient.clientWorld,
						getBumpPositions(0, 0.15, 0),
						Direction.UP,
						bumpingRule.CEILINGS
				);
			}
			if (marioClient.horizontalCollision && bumpingRule.WALLS > 0) {

			}
		}

		getAction().attemptTransitions(this, TransitionPhase.POST_MOVE);

		applyModifiedVelocity();

		marioClient.updateLimbs(false);
		return !marioClient.hasVehicle();
	}

	private Iterable<BlockPos> getBumpPositions(double stretchX, double stretchY, double stretchZ) {
		return () -> new BlockCollisionSpliterator<>(
				marioClient.getWorld(),
				marioClient,
				marioClient.getBoundingBox().stretch(stretchX, stretchY, stretchZ),
				false,
				(pos, voxelShape) -> pos);
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
