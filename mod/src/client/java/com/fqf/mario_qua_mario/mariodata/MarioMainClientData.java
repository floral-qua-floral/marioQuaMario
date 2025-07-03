package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.fqf.mario_qua_mario.util.VoxelShapeWithBlockPos;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;

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

		this.moveWithFluidPushing();
//		this.attemptBaps(this.getMario().getPos(), this.getMario().getBoundingBox(), this.moveWithFluidPushing());

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		this.applyModifiedVelocity();

		this.getMario().updateLimbs(false);

		if(this.getActionCategory() == ActionCategory.GROUNDED && this.getMario().isOnGround() && this.getYVel() == 0.0)
			this.getMario().setVelocity(this.getMario().getVelocity().withAxis(Direction.Axis.Y, -0.1));
		// ^ Needed for Presence Footsteps compatibility
		// TODO: Prevent Presence Footsteps steps while in a Sliding action.

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

	private void attemptBaps(Vec3d prevPos, Box prevBoundingBox, Vec3d motion) {
		int ceilingBapStrength = 10;
		int wallBapStrength = 10;
		int floorBapStrength = 10;

		ClientPlayerEntity mario = this.getMario();

		if(ceilingBapStrength <= 0 && wallBapStrength <= 0 && floorBapStrength <= 0) return;

		if(mario.verticalCollision) {
			if(motion.y > 0) {
				if(ceilingBapStrength > 0) {
					this.getCollidedBlocks(prevBoundingBox, Direction.Axis.Y, 2).forEach(blockPos ->
							BlockBappingUtil.attemptBap(this, mario.getWorld(), blockPos, Direction.UP, ceilingBapStrength));
				}
			}
			else {
				if(floorBapStrength > 0) {

				}
			}
		}
		double bapSpeedThreshold = 0;
		if(mario.horizontalCollision && wallBapStrength > 0 && motion.horizontalLengthSquared() > bapSpeedThreshold * bapSpeedThreshold) {
			boolean bapped = false;
			boolean processXFirst = Math.abs(motion.x) > Math.abs(motion.z);
			if(processXFirst && Math.abs(motion.x) > bapSpeedThreshold) {
				bapped = this.attemptHorizontalBapping(motion.x, wallBapStrength, prevBoundingBox, Direction.Axis.X);
			}
			if(!bapped && Math.abs(motion.z) > bapSpeedThreshold) {
				bapped = this.attemptHorizontalBapping(motion.z, wallBapStrength, prevBoundingBox, Direction.Axis.Z);
			}
			if(!bapped) {
				this.attemptHorizontalBapping(motion.x, wallBapStrength, prevBoundingBox, Direction.Axis.X);
			}
		}
	}

	private boolean attemptHorizontalBapping(double speed, int strength, Box prevBoundingBox, Direction.Axis axis) {
		boolean bapped = false;
		Direction dir = Direction.from(axis, speed > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
		for (BlockPos collidedBlock : this.getCollidedBlocks(prevBoundingBox, axis, speed)) {
			bapped = true;
			BlockBappingUtil.attemptBap(this, this.getMario().getWorld(), collidedBlock, dir, strength);
		}
		return bapped;
	}

	private Iterable<BlockPos> getCollidedBlocks(Box initialBoundingBox, Direction.Axis axis, double distance) {
		return () -> new BlockCollisionSpliterator<>(this.getMario().getWorld(), this.getMario(),
				this.getMario().getBoundingBox().stretch(Vec3d.ZERO.withAxis(axis, 0.2 * Math.signum(distance))), false,
				(mutable, voxelShape) -> mutable.toImmutable());
	}

//	private Iterable<BlockPos> getCollidedBlocks(Box initialBoundingBox, Direction.Axis axis, double distance) {
//		ImmutableCollection.Builder<BlockPos> builder = ImmutableList.builderWithExpectedSize(1);
//
//
//		Box stretchedBox = initialBoundingBox.stretch(new Vec3d(0, 2, 0));
//		Iterable<PositionedVoxelShape> positionedShapes = this.getBlockCollisionsWithPositions(stretchedBox);
//
//		double smallestValue = distance;
//		for(PositionedVoxelShape positionedShape : positionedShapes) {
//			positionedShape.value = positionedShape.SHAPE.calculateMaxDistance(axis, stretchedBox, 0.3);
//			if(Math.abs(positionedShape.value) < Math.abs(smallestValue)) smallestValue = positionedShape.value;
//			MarioQuaMario.LOGGER.info("COLLISION CHECKING\nValue for {}:\n\t{}",
//					this.getMario().getWorld().getBlockState(positionedShape.POS), positionedShape.value);
//		}
//
//		MarioQuaMario.LOGGER.info("Smallest value gotten was {}", smallestValue);
//
//		return builder.build();
//	}
//
//	private Iterable<PositionedVoxelShape> getBlockCollisionsWithPositions(
//			Box box
//	) {
//		return () -> new BlockCollisionSpliterator<>(this.getMario().getWorld(), this.getMario(), box, false, PositionedVoxelShape::new);
//	}

	public boolean doBappingCollisions() {
		return this.isEnabled() && this.getMario().getWeaponStack().isOf(Items.TRIDENT);
	}

//	private static class PositionedVoxelShape {
//		private final BlockPos POS;
//		private final VoxelShape SHAPE;
//		private double value;
//
//		private PositionedVoxelShape(BlockPos.Mutable pos, VoxelShape shape) {
//			this.POS = pos.toImmutable();
//			this.SHAPE = shape;
//		}
//	}
}
