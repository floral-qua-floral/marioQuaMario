package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMoveableData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
	private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public float strideDistance;

	@Shadow public float prevStrideDistance;

	@Override public void setSwimming(boolean swimming) {
		if(swimming && MarioDataManager.getMarioData(this).isEnabled()) return;
		super.setSwimming(swimming);
	}

	@Override
	public boolean isInSneakingPose() {
		return !MarioDataManager.getMarioData(this).isSneakProhibited() && super.isInSneakingPose();
	}

	@Override
	public void setPose(EntityPose pose) {
		super.setPose(MarioDataManager.getMarioData(this).isSneakProhibited() ? EntityPose.STANDING : pose);
	}

	@Unique
	public final double CLIPPING_LENIENCY = 0.33;

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		if(movementType == MovementType.SELF || movementType == MovementType.PLAYER) {
			if(movement.y > 0 && MarioDataManager.getMarioData(this).useMarioPhysics()) {
				// If Mario's horizontal velocity is responsible for him clipping a ceiling, then just cancel his horizontal movement
				if(
						(movement.x != 0 || movement.z != 0)
								&& getWorld().isSpaceEmpty(this, getBoundingBox().offset(movement.x, 0, movement.z))
								&& !getWorld().isSpaceEmpty(this, getBoundingBox().offset(movement))) {
					movement = new Vec3d(0, movement.y, 0);
				}

				else if(!getWorld().isSpaceEmpty(this, getBoundingBox().offset(0, movement.y, 0))) {
					Box stretchedBox = getBoundingBox().stretch(0, movement.y, 0);
					if(getWorld().isSpaceEmpty(this, stretchedBox.offset(CLIPPING_LENIENCY, 0, 0))) {
						movement = new Vec3d(movement.x - CLIPPING_LENIENCY, movement.y, movement.z);
						move(MovementType.SELF, new Vec3d(CLIPPING_LENIENCY, 0, 0));
					}
					if(getWorld().isSpaceEmpty(this, stretchedBox.offset(-CLIPPING_LENIENCY, 0, 0))) {
						movement = new Vec3d(movement.x + CLIPPING_LENIENCY, movement.y, movement.z);
						move(MovementType.SELF, new Vec3d(-CLIPPING_LENIENCY, 0, 0));
					}
					if(getWorld().isSpaceEmpty(this, stretchedBox.offset(0, 0, CLIPPING_LENIENCY))) {
						movement = new Vec3d(movement.x, movement.y, movement.z - CLIPPING_LENIENCY);
						move(MovementType.SELF, new Vec3d(0, 0, CLIPPING_LENIENCY));
					}
					if(getWorld().isSpaceEmpty(this, stretchedBox.offset(0, 0, -CLIPPING_LENIENCY))) {
						movement = new Vec3d(movement.x, movement.y, movement.z + CLIPPING_LENIENCY);
						move(MovementType.SELF, new Vec3d(0, 0, -CLIPPING_LENIENCY));
					}
				}
			}
		}

		super.move(movementType, movement);
	}

	@Override
	public boolean startRiding(Entity entity, boolean force) {
		boolean startedRiding = super.startRiding(entity, force);

		MarioPlayerData data = MarioDataManager.getMarioData(this);

		data.attemptDismount = false;
		data.setActionTransitionless(RegistryManager.ACTIONS.get(Identifier.of("qua_mario:mounted")));

		return startedRiding;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		return super.writeNbt(nbt);
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data instanceof MarioMoveableData moveableData && data.useMarioPhysics()
				&& moveableData.travelHook(movementInput.z, movementInput.x))
			ci.cancel();
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaJumpingSwimming(CallbackInfoReturnable<Boolean> cir) {
		if(MarioDataManager.getMarioData(this).useMarioPhysics())
			cir.setReturnValue(false);
	}

	@Inject(at = @At("TAIL"), method = "getBaseDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;", cancellable = true)
	private void getBaseDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.isEnabled()) {
			// Returns the standing hitbox if being used by Mario while he can't sneak
			if(data.isSneakProhibited() && pose == EntityPose.CROUCHING) {
				cir.setReturnValue(data.getMario().getBaseDimensions(EntityPose.STANDING));
				return;
			}

			ParsedPowerUp powerUp = data.getPowerUp();
			ParsedCharacter character = data.getCharacter();

			float widthFactor = powerUp.WIDTH_FACTOR * character.WIDTH_FACTOR;
			float heightFactor = powerUp.HEIGHT_FACTOR * character.HEIGHT_FACTOR;
			if(pose == EntityPose.CROUCHING) heightFactor *= 0.6F;

//			float eyeHeightOffset = 0;
//			if(data.isClient() && data.getMario().isMainPlayer() &&
//					BumpManager.eyeAdjustmentParticle != null && BumpManager.eyeAdjustmentParticle.isAlive()) {
//				eyeHeightOffset = BumpManager.eyeAdjustmentParticle.lastOffset;
//				MarioQuaMario.LOGGER.info("Found it!\n{}\n{}", BumpManager.eyeAdjustmentParticle, BumpManager.eyeAdjustmentParticle.lastOffset);
//			}

			EntityDimensions resultDimensions = cir.getReturnValue();

			cir.setReturnValue(new EntityDimensions(
					resultDimensions.width() * widthFactor,
					resultDimensions.height() * heightFactor,
					resultDimensions.eyeHeight() * heightFactor,
					resultDimensions.attachments(), resultDimensions.fixed()
			));
		}
	}

	@Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
	public void slideOffLedges(CallbackInfoReturnable<Boolean> cir) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.useMarioPhysics() && data.getAction().SNEAK_LEGALITY.slipOffLedges()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "tickMovement", at = @At("TAIL"))
	public void preventViewBobbing(CallbackInfo ci) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.isClient() && !data.getAction().SLIDING_STATUS.doViewBobbing()) {
			strideDistance = prevStrideDistance * 0.6F;
		}
	}

	@Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
	public void changeDismounting(CallbackInfoReturnable<Boolean> cir) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.isEnabled()) {
			cir.setReturnValue(data.attemptDismount);
			if(data.attemptDismount) data.attemptDismount = false;
		}
	}

	@WrapWithCondition(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;jump()V"))
	private boolean preventLivingEntityJump(LivingEntity instance) {
		return !MarioDataManager.getMarioData(this).useMarioPhysics();
	}


}
