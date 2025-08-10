package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.fqf.mario_qua_mario.util.BlockCollisionFinder;
import com.fqf.mario_qua_mario.util.DirectionBasedWallInfo;
import com.fqf.mario_qua_mario.util.AdvancedWallInfo;
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
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.MovementType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
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

	@Override public void tick() {
		super.tick();
		this.getAction().clientTick(this, true);
		this.getPowerUp().clientTick(this, true);
		this.getCharacter().clientTick(this, true);
		if(!MinecraftClient.getInstance().options.getPerspective().isFirstPerson())
			this.currentCameraAnimation = null;
	}

	private final WallInfoWithInputs WALL_INFO = new WallInfoWithInputs(this);
	private static class WallInfoWithInputs extends DirectionBasedWallInfo {
		private double towardsWallInput;
		private double sidleInput;
		private boolean calculatedInputs;

		protected WallInfoWithInputs(MarioPlayerData owner) {
			super(owner);
		}

		private void calculateInputs() {
			if(this.calculatedInputs) return;
			this.calculatedInputs = true; // Only calculate once per tick

			float yawRadians = this.OWNER.getMario().getYaw() * MathHelper.RADIANS_PER_DEGREE;
			float sinYaw = MathHelper.sin(yawRadians);
			float cosYaw = MathHelper.cos(yawRadians);
			Vector3f forwardDir = new Vector3f(-sinYaw, 0, cosYaw);
			Vector3f rightDir = new Vector3f(cosYaw, 0, sinYaw);

			// Vector for inputs in world space
			Vector3f worldSpaceInputs = forwardDir.mul((float) this.OWNER.getInputs().getForwardInput())
					.add(rightDir.mul((float) this.OWNER.getInputs().getStrafeInput()));
//			MarioQuaMario.LOGGER.info("World-space inputs: ({}, {}, {})", worldSpaceInputs.x, worldSpaceInputs.y, worldSpaceInputs.z);

			this.towardsWallInput = MathHelper.clamp(worldSpaceInputs.dot(this.wallDirection.getUnitVector()), -1, 1);
			this.sidleInput = MathHelper.clamp(worldSpaceInputs.dot(this.wallDirection.rotateYClockwise().getUnitVector()), -1, 1);
		}

		@Override
		public double getTowardsWallInput() {
			calculateInputs();
			return this.towardsWallInput;
		}

		@Override
		public double getSidleInput() {
			calculateInputs();
			return this.sidleInput;
		}
	}

	@Override
	public AdvancedWallInfo getWallInfo() {
		return this.WALL_INFO;
	}

	public void tickInputs() {
		this.INPUTS.updateButtons();
	}

	@Override public boolean travelHook(double forwardInput, double strafeInput) {
		this.INPUTS.updateAnalog(forwardInput, strafeInput);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);

		boolean cancelVanillaTravel = this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();

		this.RECORDED_COLLISIONS.clear();
		this.RECORDED_COLLISIONS.storedVelocity = this.getMario().getVelocity();
		this.RECORDED_COLLISIONS.COLLIDED[0] = false; this.RECORDED_COLLISIONS.REFLECTS[0] = false;
		this.RECORDED_COLLISIONS.COLLIDED[1] = false; this.RECORDED_COLLISIONS.REFLECTS[1] = false;
		this.RECORDED_COLLISIONS.COLLIDED[2] = false; this.RECORDED_COLLISIONS.REFLECTS[2] = false;
		Vec3d movement = this.getMovementWithFluidPushing();
		this.preemptMovement(movement);
		this.getMario().move(MovementType.SELF, movement);

		this.WALL_INFO.calculatedInputs = false;

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		this.applyModifiedVelocity();

		if(this.getActionCategory() == ActionCategory.GROUNDED && this.getMario().isOnGround() && this.getYVel() == 0.0)
			this.getMario().setVelocity(this.getMario().getVelocity().withAxis(Direction.Axis.Y, -0.1));
		// ^ Needed for Presence Footsteps compatibility
		// TODO: Prevent Presence Footsteps steps while in a Sliding action.

		this.getMario().updateLimbs(false);

		return cancelVanillaTravel;
	}

	@Override
	public void handleInputUnbuffering(boolean transitionSuccessful) {
		if(transitionSuccessful) this.INPUTS.conditionallyUnbufferAll();
		else this.INPUTS.cancelUnbuffers();
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
			private boolean shouldUnbuffer = false;

			@Override public boolean isHeld() {
				return this.isHeld;
			}
			@Override public boolean isPressed() {
				return this.isPressedNoUnbuffer() && this.planUnbuffer();
			}
			public boolean isPressedNoUnbuffer() {
				return this.pressBuffer > 0;
			}

			private boolean planUnbuffer() {
				this.shouldUnbuffer = true;
				return true;
			}
			private void conditionallyUnbuffer() {
				if(this.shouldUnbuffer) {
					this.unbuffer();
				}
			}
			private void unbuffer() {
				this.pressBuffer = 0;
				this.shouldUnbuffer = false;
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

		private void cancelUnbuffers() {
			this.JUMP_CLIENT.shouldUnbuffer = false;
			this.DUCK_CLIENT.shouldUnbuffer = false;
			this.SPIN_CLIENT.shouldUnbuffer = false;
		}

		private void conditionallyUnbufferAll() {
			this.JUMP_CLIENT.conditionallyUnbuffer();
			this.DUCK_CLIENT.conditionallyUnbuffer();
			this.SPIN_CLIENT.conditionallyUnbuffer();
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
		private final MarioMainClientData OWNER;
		private Vec3d storedVelocity;
		private final boolean[] COLLIDED = new boolean[3];
		private final boolean[] REFLECTS = new boolean[3];

		private MainClientRecordedCollisions(MarioMainClientData owner) {
			OWNER = owner;
		}

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

		private double absSpeedOnAxis(@NotNull Direction.Axis axis) {
			return Math.abs(this.storedVelocity.getComponentAlongAxis(axis));
		}

		private Direction.Axis chooseGreaterAxis(@Nullable Direction.Axis alfa, @Nullable Direction.Axis bravo) {
			if(alfa == null) return bravo;
			if(bravo == null) return alfa;
			return absSpeedOnAxis(alfa) > absSpeedOnAxis(bravo) ? alfa : bravo;
		}

		@Override
		public @Nullable Direction getDirectionOfCollisionsWith(CollisionMatcher matcher, boolean allowVertical) {
			boolean onXAxis = false;
			boolean onYAxis = false;
			boolean onZAxis = false;

			for(RecordedCollision recordedCollision : this) {
				if(switch(recordedCollision.direction().getAxis()) {
					case X -> onXAxis;
					case Y -> onYAxis || !allowVertical;
					case Z -> onZAxis;
				}) continue;

				if(matcher.test(recordedCollision, this.OWNER.getMario().getWorld().getBlockState(recordedCollision.pos()))) {
					switch(recordedCollision.direction().getAxis()) {
						case X -> onXAxis = true;
						case Y -> onYAxis = true;
						case Z -> onZAxis = true;
					}
				}
			}

			Direction.Axis greatestAxis = this.chooseGreaterAxis(onXAxis ? Direction.Axis.X : null, this.chooseGreaterAxis(onYAxis ? Direction.Axis.Y : null, onZAxis ? Direction.Axis.Z : null));

			return greatestAxis == null ? null : Direction.from(greatestAxis, this.storedVelocity.getComponentAlongAxis(greatestAxis) > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
		}

		@Override
		public @Nullable RecordedCollision getAnyMatch(CollisionMatcher matcher) {
			for(RecordedCollision recordedCollision : this) {
				if(matcher.test(recordedCollision, this.OWNER.getMario().getWorld().getBlockState(recordedCollision.pos())))
					return recordedCollision;
			}
			return null;
		}
	}

	private final MainClientRecordedCollisions RECORDED_COLLISIONS = new MainClientRecordedCollisions(this);

	@Override
	public RecordedCollisionSet getRecordedCollisions() {
		return this.RECORDED_COLLISIONS;
	}

	private void preemptMovement(Vec3d movement) {
		boolean shouldRecalculate;
		do {
			shouldRecalculate = preemptMovementAndOptionallyRecalculate(movement);
		}
		while(shouldRecalculate);
	}

	private boolean preemptMovementAndOptionallyRecalculate(Vec3d movement) {
		Box movedBox = bapAlongAxis(this.getMario().getBoundingBox(), movement, Direction.Axis.Y);
		if(movedBox == null) return true;

		boolean doXFirst = Math.abs(movement.x) > Math.abs(movement.z);
		if(doXFirst) {
			movedBox = bapAlongAxis(movedBox, movement, Direction.Axis.X);
			if(movedBox == null) return true;
		}
		movedBox = bapAlongAxis(movedBox, movement, Direction.Axis.Z);
		if(movedBox == null) return true;
		if(!doXFirst) {
			movedBox = bapAlongAxis(movedBox, movement, Direction.Axis.X);
			return movedBox == null;
		}
		return false;
	}

	private Box bapAlongAxis(Box box, Vec3d movement, Direction.Axis axis) {
		double motion = movement.getComponentAlongAxis(axis);
		if(Math.abs(motion) < 1.0E-7) return box;

		Direction dir = Direction.from(axis, motion > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);

		ObjectDoublePair<Set<BlockPos>> pair = BlockCollisionFinder.getCollidedBlockPositions(this.getMario(), box, motion, axis);
		Set<BlockPos> collideWithBlockPositions = pair.left();
		double absSmallestOffsetFound = pair.rightDouble();

		if(!collideWithBlockPositions.isEmpty()) {
			for(BlockPos pos : collideWithBlockPositions) {
				if(this.collideWithBlockAndOptionallyRecalculate(pos, dir))
					return null; // Force recalculation from the beginning
			}
		}

		return box.offset(Vec3d.ZERO.withAxis(axis, absSmallestOffsetFound * Math.signum(motion)));
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
		this.RECORDED_COLLISIONS.add(new RecordedCollision(pos, this.getMario().clientWorld.getBlockState(pos), direction, bapResult));
		return bapResult == BapResult.BUST;
	}
}
