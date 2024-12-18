package com.floralquafloral.mariodata.moveable;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.bumping.BumpManagerClient;
import com.floralquafloral.mariodata.MarioClientSideDataImplementation;
import com.floralquafloral.definitions.actions.ActionDefinition;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.action.TransitionPhase;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockCollisionSpliterator;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MarioMainClientData extends MarioMoveableData implements MarioClientSideDataImplementation {
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

	public MarioMainClientData(PlayerEntity mario) {
		super(mario);
		this.marioClient = (ClientPlayerEntity) mario;
		this.INPUTS = new ClientInputs();
		MarioMainClientData.instance = this;
	}

	@Override
	public void setAction(ParsedAction action, long seed) {
		MarioQuaMario.LOGGER.warn("Triggered setAction with transitions on the main client. This is abnormal!");
		super.setAction(action, seed);
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

		getAction().attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		getAction().attemptTransitions(this, TransitionPhase.PRE_TRAVEL);
		getAction().travelHook(this);
		getAction().attemptTransitions(this, TransitionPhase.INPUT);

		applyModifiedVelocity();
		Vec3d storedVelocity = marioClient.getVelocity();
		Box storedBoundingBox = marioClient.getBoundingBox();
		marioClient.move(MovementType.SELF, storedVelocity);

		getTimers().bumpedFloor = false;
		getTimers().bumpedCeiling = false;
		getTimers().bumpedWall = null;
		ActionDefinition.BumpingRule bumpingRule = getAction().BUMPING_RULE;
		if(bumpingRule != null) {
			if (marioClient.verticalCollision) {
				if (marioClient.groundCollision) {
					if (bumpingRule.FLOORS > 0) getTimers().bumpedFloor = BumpManagerClient.attemptBumpBlocks(
							this,
							marioClient.clientWorld,
							getBumpPositions(storedBoundingBox.stretch(0, storedVelocity.y, 0), Direction.DOWN),
							Direction.DOWN,
							bumpingRule.FLOORS
					);
				} else if (bumpingRule.CEILINGS > 0) {
					getTimers().bumpedCeiling = false;
					getTimers().bumpedCeiling = BumpManagerClient.attemptBumpBlocks(
							this,
							marioClient.clientWorld,
							getBumpPositions(storedBoundingBox.stretch(0, storedVelocity.y, 0), Direction.UP),
							Direction.UP,
							bumpingRule.CEILINGS
					) || getTimers().bumpedCeiling;
				}
			}
			if (marioClient.horizontalCollision && bumpingRule.WALLS > 0) {
				if(Math.abs(storedVelocity.x) > bumpingRule.WALL_SPEED_THRESHOLD.get(this)) {
					if (storedVelocity.x > 0)
						attemptHorizontalBump(Direction.EAST, storedBoundingBox, storedVelocity, bumpingRule.WALLS);
					else
						attemptHorizontalBump(Direction.WEST, storedBoundingBox, storedVelocity, bumpingRule.WALLS);
				}

				if(Math.abs(storedVelocity.z) > bumpingRule.WALL_SPEED_THRESHOLD.get(this)) {
					if (storedVelocity.z > 0)
						attemptHorizontalBump(Direction.SOUTH, storedBoundingBox, storedVelocity, bumpingRule.WALLS);
					else
						attemptHorizontalBump(Direction.NORTH, storedBoundingBox, storedVelocity, bumpingRule.WALLS);
				}
			}
		}

		getAction().attemptTransitions(this, TransitionPhase.WORLD_COLLISION); // this occurs twice per tick
		applyModifiedVelocity();

		getTimers().jumpLandingTime--;
		getTimers().doubleJumpLandingTime--;
		getTimers().actionInterceptedAttack = false;

		marioClient.updateLimbs(false);
		return !marioClient.hasVehicle();
	}

	private Set<BlockPos> getBumpPositions(Box boundingBox, Direction direction) {
		Iterable<BlockPos> iterable = () -> new BlockCollisionSpliterator<>(
				marioClient.getWorld(),
				marioClient,
				boundingBox,
				false,
				(pos, voxelShape) -> pos.toImmutable());

		HashSet<BlockPos> positions = new HashSet<>();
		for(BlockPos position : iterable) positions.add(position);
		Set<BlockPos> allPositionsChecked = new HashSet<>(positions);
		positions.removeIf(blockPos -> allPositionsChecked.contains(blockPos.offset(direction.getOpposite())));
		return positions;
	}

	private void attemptHorizontalBump(Direction direction, Box storedBoundingBox, Vec3d storedVelocity, int bumpStrength) {
		boolean isXAxis = direction.getAxis() == Direction.Axis.X;
		Vec3d preBumpVelocity = marioClient.getVelocity();
		if(BumpManagerClient.attemptBumpBlocks(
				this,
				marioClient.clientWorld,
				getBumpPositions(storedBoundingBox.stretch(isXAxis ? storedVelocity.x : 0, 0, isXAxis ? 0 : storedVelocity.z), direction),
				direction,
				bumpStrength
		)) {
			boolean velocityChangedByBump = marioClient.getVelocity() != preBumpVelocity;
			Vec3d velocityToStore = velocityChangedByBump ? marioClient.getVelocity().multiply(isXAxis ? -1 : 1, 1, isXAxis ? 1 : -1) : storedVelocity;
			MarioQuaMario.LOGGER.info("horizontal bump vel:"
					+ "\nPreBumpVel: " + preBumpVelocity
					+ "\nvel: " + marioClient.getVelocity()
					+ "\nchanged: " + velocityChangedByBump
					+ "\nstore: " + velocityToStore
					+ "\nstored: " + storedVelocity
			);
			this.getTimers().bumpedWall = new Pair<>(direction, velocityToStore);
		}
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

	public boolean canRepeatPound = true;
}
