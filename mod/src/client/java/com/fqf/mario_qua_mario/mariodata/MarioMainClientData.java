package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.fqf.mario_qua_mario.util.WallInfoWithMove;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import com.fqf.mario_qua_mario_api.mariodata.util.RecordedCollision;
import com.fqf.mario_qua_mario_api.mariodata.util.RecordedCollisionSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

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
	private boolean readyForReplacement;

	@Override public void setActionTransitionless(AbstractParsedAction action) {
		this.handleSlidingSound(action);
		this.MARIO.mqm$getAnimationData().replaceAnimation(this, action.ANIMATION, -1);

		if(action != this.getAction()) {
			this.playCameraAnimation(action.CAMERA_ANIMATIONS);
		}

		super.setActionTransitionless(action);
	}

	@Override
	public void playCameraAnimation(CameraAnimationSet animationSet) {
		CameraAnimation newAnim = animationSet == null ? null : switch (animationSet.optionGetter().get()) {
			case AUTHENTIC -> animationSet.authentic();
			case GENTLE -> {
				CameraAnimation gentleAnimation = animationSet.gentle();
				yield gentleAnimation == null ? animationSet.authentic() : gentleAnimation;
			}
			case MINIMAL -> animationSet.minimal();
		};

		if(newAnim == this.currentCameraAnimation && !this.readyForReplacement) return;

		if(newAnim != null) {
			this.currentCameraAnimation = newAnim;
			this.cameraAnimationTime = 0;
			this.attemptFinish = false;
			this.readyForReplacement = false;
		}
		else this.attemptFinish = true;
	}

	public boolean animatingCamera() {
		return this.isEnabled() && this.currentCameraAnimation != null && !this.getMario().isSleeping();
	}

	public void mutateCamera(Arrangement cameraArrangement, float tickDelta) {
		this.cameraAnimationTime += MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
		float progress = this.currentCameraAnimation.progressHandler().progressCalculator().calculateProgress(this, this.cameraAnimationTime);

		this.readyForReplacement = progress >= this.currentCameraAnimation.progressHandler().minProgressToFinish();

		if(this.attemptFinish && this.readyForReplacement) {
			this.currentCameraAnimation = null;
			this.attemptFinish = false;
		}
		else this.getMario().mqm$getAnimationData().mutate(cameraArrangement, this.currentCameraAnimation.mutator(), this, progress);
	}

	private float clampYawAround = 0;
	private float prevClampYawAround = 0;
	private float clampYawHeadRange = 360;
	private float prevClampYawHeadRange = 360;

	@Override public void tick() {
		super.tick();
		this.getAction().clientTick(this, true);
		this.getPowerUp().clientTick(this, true);
		this.getCharacter().clientTick(this, true);
		if(!MinecraftClient.getInstance().options.getPerspective().isFirstPerson())
			this.currentCameraAnimation = null;

		this.prevClampYawAround = this.clampYawAround;
		this.prevClampYawHeadRange = this.clampYawHeadRange;
		this.clampYawAround = this.getMario().getWorld().getTime();
		this.clampYawHeadRange = this.getMario().isSneaking() ? 40 : 360;
	}

	private final WallInfoFromCollisions WALL_INFO = new WallInfoFromCollisions(this);
	private static class WallInfoFromCollisions implements WallInfoWithMove {
		private final MarioMainClientData OWNER;
		private @Nullable Direction wallDirection = null;
		private double towardsWallInput;
		private double sidleInput;
		private boolean calculatedInputs;

		private WallInfoFromCollisions(MarioMainClientData owner) {
			OWNER = owner;
		}

		private void update() {
			this.calculatedInputs = false;
			if(this.OWNER.RECORDED_COLLISIONS.isEmpty()) {
				wallDirection = null;
				return;
			}

			Direction.Axis biggestAxis;
			Vec3d storedVelocity = this.OWNER.RECORDED_COLLISIONS.storedVelocity;

			if(
					this.wallDirection != null && this.OWNER.RECORDED_COLLISIONS.collidedOnAxis(this.wallDirection.getAxis())
					&& Math.signum(storedVelocity.getComponentAlongAxis(this.wallDirection.getAxis())) != wallDirection.getDirection().offset()
			) {
				biggestAxis = this.wallDirection.getAxis();
			}
			else if(this.OWNER.RECORDED_COLLISIONS.collidedOnAxis(Direction.Axis.X) && this.OWNER.RECORDED_COLLISIONS.collidedOnAxis(Direction.Axis.Z)) {
				if(Math.abs(storedVelocity.x) > Math.abs(storedVelocity.z))
					biggestAxis = Direction.Axis.X;
				else
					biggestAxis = Direction.Axis.Z;
			}
			else if(this.OWNER.RECORDED_COLLISIONS.collidedOnAxis(Direction.Axis.X))
				biggestAxis = Direction.Axis.X;
			else if(this.OWNER.RECORDED_COLLISIONS.collidedOnAxis(Direction.Axis.Z))
				biggestAxis = Direction.Axis.Z;
			else
				biggestAxis = null;

			if(biggestAxis == null) this.wallDirection = null;
			else this.wallDirection = Direction.from(biggestAxis, // Direction is pointing towards the wall
					storedVelocity.getComponentAlongAxis(biggestAxis) > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
		}

		private void assignDirection(Direction direction) {
			this.calculatedInputs = false;
			this.wallDirection = direction;
		}

		@Override
		public Vec3d getWallNormal() {
			assert this.wallDirection != null;
			return new Vec3d(this.wallDirection.getOpposite().getUnitVector());
		}

		@Override
		public float getWallYaw() {
			assert this.wallDirection != null;
			return switch(this.wallDirection.getOpposite()) {
				case NORTH -> 180;
				case SOUTH -> 0;
				case WEST -> 90;
				case EAST -> -90;
				default -> throw new IllegalStateException("Illegal wall direction: " + this.wallDirection + " :(");
			};
		}

		private void calculateInputs() {
			assert this.wallDirection != null;

			if(this.calculatedInputs) return;
			this.calculatedInputs = true; // Only calculate once per tick

			float yawRadians = this.OWNER.getMario().getYaw() * MathHelper.RADIANS_PER_DEGREE;
			float sinYaw = MathHelper.sin(yawRadians);
			float cosYaw = MathHelper.cos(yawRadians);
			Vector3f forwardDir = new Vector3f(-sinYaw, 0, cosYaw);
			Vector3f rightDir = new Vector3f(cosYaw, 0, sinYaw);

			// Raw input vector in world space
			Vector3f worldSpaceInputs = forwardDir.mul((float) this.OWNER.getInputs().getForwardInput())
					.add(rightDir.mul((float) this.OWNER.getInputs().getStrafeInput()));
//			MarioQuaMario.LOGGER.info("Worldspace inputs: ({}, {}, {})", worldSpaceInputs.x, worldSpaceInputs.y, worldSpaceInputs.z);

			this.towardsWallInput = worldSpaceInputs.dot(this.wallDirection.getUnitVector());
			this.sidleInput = worldSpaceInputs.dot(this.wallDirection.rotateYClockwise().getUnitVector());
		}

		@Override
		public double getTowardsWallInput() {
			this.calculateInputs();
			assert this.wallDirection != null;
			return this.towardsWallInput;
		}

		@Override
		public double getSidleInput() {
			assert this.wallDirection != null;
			return this.sidleInput;
		}

		@Override
		public double getTowardsWallVel() {
			assert this.wallDirection != null;
			return this.OWNER.getVelocity().getComponentAlongAxis(this.wallDirection.getAxis()) * this.wallDirection.getDirection().offset();
		}

		@Override
		public double getSidleVel() {
			assert this.wallDirection != null;
			Direction sidleDirection = this.wallDirection.rotateYClockwise();
			return this.OWNER.getVelocity().getComponentAlongAxis(sidleDirection.getAxis()) * sidleDirection.getDirection().offset();
		}

		@Override
		public void setTowardsWallVel(double velocity) {
			assert this.wallDirection != null;
			this.OWNER.setVelocity(this.OWNER.getVelocity().withAxis(this.wallDirection.getAxis(), velocity * this.wallDirection.getDirection().offset()));
		}

		@Override
		public void setSidleVel(double velocity) {
			assert this.wallDirection != null;
			Direction sidleDirection = this.wallDirection.rotateYClockwise();
			this.OWNER.setVelocity(this.OWNER.getVelocity().withAxis(sidleDirection.getAxis(), velocity * sidleDirection.getDirection().offset()));
		}
	}

	@Override
	public void assignWallDirection(Direction direction) {
		this.WALL_INFO.assignDirection(direction);
	}

	@Override
	public @Nullable WallInfoWithMove getWallInfo() {
		return this.WALL_INFO.wallDirection == null ? null : this.WALL_INFO;
	}

	public void tickInputs() {
		this.INPUTS.updateButtons();
	}

	@Override public boolean travelHook(double forwardInput, double strafeInput) {
		this.INPUTS.updateAnalog(forwardInput, strafeInput);

//		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);

		boolean cancelVanillaTravel = this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();

		this.RECORDED_COLLISIONS.clear();
		this.RECORDED_COLLISIONS.storedVelocity = this.getMario().getVelocity();
		this.RECORDED_COLLISIONS.COLLIDED[0] = false; this.RECORDED_COLLISIONS.REFLECTS[0] = false;
		this.RECORDED_COLLISIONS.COLLIDED[1] = false; this.RECORDED_COLLISIONS.REFLECTS[1] = false;
		this.RECORDED_COLLISIONS.COLLIDED[2] = false; this.RECORDED_COLLISIONS.REFLECTS[2] = false;
		this.moveWithFluidPushing();

		this.WALL_INFO.update();

//		if(this.getWallInfo() != null) {
//			if(this.getMario().isSneaking()) this.getWallInfo().setTowardsWallVel(1);
//			MarioQuaMario.LOGGER.info("Wall input testing.\n\tTowards: {}\n\tSidle: {}",
//					this.getWallInfo().getTowardsWallInput(), this.getWallInfo().getSidleInput());
//		}

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		this.applyModifiedVelocity();

		if(this.getActionCategory() == ActionCategory.GROUNDED && this.getMario().isOnGround() && this.getYVel() == 0.0)
			this.getMario().setVelocity(this.getMario().getVelocity().withAxis(Direction.Axis.Y, -0.1));
		// ^ Needed for Presence Footsteps compatibility
		// TODO: Prevent Presence Footsteps steps while in a Sliding action.

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

	@Override
	public int getBapStrength(AbstractParsedAction action, Direction direction) {
		int strength = super.getBapStrength(action, direction);

		if(strength != 0 && direction.getAxis().isHorizontal()) {
			Vec3d marioVel = this.getMario().getVelocity();

			double wallSpeedThreshold = this.getAction().BAPPING_RULE.wallBumpSpeedThreshold().getAsThreshold(this);
			if(marioVel.horizontalLengthSquared() < wallSpeedThreshold * wallSpeedThreshold)
				return 0;
			if(Math.abs(marioVel.getComponentAlongAxis(direction.getAxis())) < wallSpeedThreshold * 0.5)
				return 0;
		}

		return strength;
	}

	private static class MainClientRecordedCollisions extends HashSet<RecordedCollision> implements RecordedCollisionSet {
		private Vec3d storedVelocity;
		private final boolean[] COLLIDED = new boolean[3];
		private final boolean[] REFLECTS = new boolean[3];

		@Override
		public boolean collidedOnAxis(Direction.Axis axis) {
			return this.COLLIDED[axis.ordinal()];
		}
		private boolean shouldReflectOnAxis(Direction.Axis axis) {
			return this.REFLECTS[axis.ordinal()];
		}

		@Override
		public Vec3d getPreCollisionVelocity() {
			return this.storedVelocity;
		}

		@Override
		public Vec3d getReflectedVelocity() {
			return this.storedVelocity.multiply(
					shouldReflectOnAxis(Direction.Axis.X) ? -1 : 1,
					shouldReflectOnAxis(Direction.Axis.Y) ? -1 : 1,
					shouldReflectOnAxis(Direction.Axis.Z) ? -1 : 1
			);
		}

		@Override
		public Vec3d getHorizontallyReflectedVelocity() {
			return this.storedVelocity.multiply(
					shouldReflectOnAxis(Direction.Axis.X) ? -1 : 1,
					1,
					shouldReflectOnAxis(Direction.Axis.Z) ? -1 : 1
			);
		}
	}

	private final MainClientRecordedCollisions RECORDED_COLLISIONS = new MainClientRecordedCollisions();

	@Override
	public RecordedCollisionSet getLastTickCollisions() {
		return this.RECORDED_COLLISIONS;
	}

	public boolean collideWithBlockAndOptionallyRecalculate(BlockPos pos, Direction direction) {
		BapResult bapResult;
		int bapStrength = this.getBapStrength(direction);
		if(bapStrength == 0)
			bapResult = null;
		else
			bapResult = BlockBappingUtil.attemptBap(this, this.getMario().clientWorld, pos, direction, bapStrength);

		if(bapResult != BapResult.BUST)
			this.RECORDED_COLLISIONS.REFLECTS[direction.getAxis().ordinal()] = true;
		this.RECORDED_COLLISIONS.COLLIDED[direction.getAxis().ordinal()] = true;
		this.RECORDED_COLLISIONS.add(new RecordedCollision(pos, direction, bapResult));
		return bapResult == BapResult.BUST;
	}

	public void onMarioLookAround() {
		ClientPlayerEntity mario = this.getMario();

		if(!mario.isSneaking()) return;

		float aroundYaw = 0;
		float maximumHeadDifference = 45;

		mario.setBodyYaw(aroundYaw);

		float headDifference = MathHelper.wrapDegrees(mario.getYaw() - aroundYaw);
		float clampedHeadYawDifference = MathHelper.clamp(headDifference, -maximumHeadDifference, maximumHeadDifference);
		mario.prevYaw += clampedHeadYawDifference - headDifference;
		mario.setYaw(mario.getYaw() + clampedHeadYawDifference - headDifference);

		mario.setHeadYaw(mario.getYaw());
	}
}
