package com.fqf.charapoweract.mixin;

import com.fqf.charapoweract.MarioQuaMario;
import com.fqf.charapoweract.bapping.BlockBappingUtil;
import com.fqf.charapoweract.bapping.WorldBapsInfo;
import com.fqf.charapoweract.compat.optional.MarioSableCompatSafe;
import com.fqf.charapoweract.cpadata.CPAMoveableData;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract.cpadata.injections.AdvCPADataHolder;
import com.fqf.charapoweract.registries.actions.AbstractParsedAction;
import com.fqf.charapoweract.registries.actions.parsed.ParsedWallboundAction;
import com.fqf.charapoweract.util.MarioGamerules;
import com.fqf.charapoweract_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charapoweract_api.definitions.states.actions.util.SlidingStatus;
import com.fqf.charapoweract_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charapoweract.util.MarioNbtKeys;
import com.fqf.charapoweract_api.definitions.states.actions.util.WallBodyAlignment;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements AdvCPADataHolder {
	@Shadow public abstract EntityDimensions getBaseDimensions(EntityPose pose);

	@Shadow public float strideDistance;
	@Shadow public float prevStrideDistance;

	private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickHook(CallbackInfo ci) {
		CPAPlayerData data = this.cpa$getCPAData();
		if(data.isEnabled()) data.tick();
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		if(this.cpa$getCPAData() instanceof CPAMoveableData moveableData
				&& moveableData.doMarioTravel()
				&& moveableData.travelHook(movementInput.z, movementInput.x)) {
			// SABLE HOOK
			MarioSableCompatSafe.trySablePostTravelCompatibility(moveableData.getPlayer());
			ci.cancel();
		}
	}

	@Override
	protected boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement) {
		return switch(this.cpa$getCPAData().isEnabled() ? this.cpa$getCPAData().getAction().SLIDING_STATUS : null) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> false;
			case null, default -> super.stepOnBlock(pos, state, playSound, emitEvent, movement);
		};
	}

	@Override
	protected void playStepSounds(BlockPos pos, BlockState state) {
		switch(this.cpa$getCPAData().isEnabled() ? this.cpa$getCPAData().getAction().SLIDING_STATUS : null) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> {}
			case null, default -> super.playStepSounds(pos, state);
		};
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaSwimming(CallbackInfoReturnable<Boolean> cir) {
		if(cpa$getCPAData().doMarioTravel())
			cir.setReturnValue(false);
	}

	@Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaFluidPushing(CallbackInfoReturnable<Boolean> cir) {
		if(cpa$getCPAData().doMarioTravel())
			cir.setReturnValue(false);
	}

	@Inject(method = "getBaseDimensions", at = @At("RETURN"), cancellable = true)
	private void alterMarioHitbox(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		CPAPlayerData data = cpa$getCPAData();
		if(data.isEnabled()) {
			// Return the standing hitbox if we're trying to evaluate Mario's sneaking hitbox while he can't sneak
			if(pose == EntityPose.CROUCHING && data.getAction().SNEAKING_RULE == SneakingRule.PROHIBIT)
				cir.setReturnValue(getBaseDimensions(EntityPose.STANDING));
			else if(pose == EntityPose.STANDING && data.getAction().SNEAKING_RULE == SneakingRule.FORCE)
				cir.setReturnValue(getBaseDimensions(EntityPose.CROUCHING));
			else {
				float widthFactor = data.getHorizontalScale();
				float heightFactor = data.getVerticalScale();
				float eyeHeightFactor = data.getEyeHeightScale();
				if(pose == EntityPose.CROUCHING) {
					heightFactor *= 0.6F;
					eyeHeightFactor *= 0.6F;
				}

				EntityDimensions normalDimensions = cir.getReturnValue();

				cir.setReturnValue(new EntityDimensions(
						normalDimensions.width() * widthFactor,
						normalDimensions.height() * heightFactor,
						normalDimensions.eyeHeight() * eyeHeightFactor,
						normalDimensions.attachments().scale(widthFactor, heightFactor, widthFactor), normalDimensions.fixed()
				));
			}
		}
	}

	@Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
	private void slideOffLedges(CallbackInfoReturnable<Boolean> cir) {
		CPAPlayerData data = cpa$getCPAData();
		if(data.doMarioTravel() && data.getAction().SNEAKING_RULE == SneakingRule.SLIP)
			cir.setReturnValue(false);
	}

	@Inject(method = "tickMovement", at = @At("TAIL"))
	private void preventViewBobbing(CallbackInfo ci) {
		CPAPlayerData data = cpa$getCPAData();
		if(data.isClient() && data.isEnabled() && data.getAction().SLIDING_STATUS != SlidingStatus.NOT_SLIDING)
			strideDistance = prevStrideDistance * 0.6F;
	}

	@Override
	public boolean startRiding(Entity entity, boolean force) {
		CPAPlayerData data = cpa$getCPAData();
		if(data.isEnabled()) {
			AbstractParsedAction mountedAction = data.getCharacter().getMountedAction(entity);
			boolean mounted = mountedAction != null && super.startRiding(entity, force);
			if (mounted) {
				data.setActionTransitionless(mountedAction);
			}
			return mounted;
		}
		return super.startRiding(entity, force);
	}

	@Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
	private void changeDismounting(CallbackInfoReturnable<Boolean> cir) {
		CPAPlayerData data = cpa$getCPAData();
		if(data.isEnabled()) cir.setReturnValue(false);
	}

//	@Override
//	protected void onDismounted(Entity vehicle) {
//		if(cpa$getCPAData().attemptDismount == CPAPlayerData.DismountType.DISMOUNT_IN_PLACE)
//			requestTeleportAndDismount(this.getX(), this.getY(), this.getZ());
//		else
//			super.onDismounted(vehicle);
//	}

	@WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean modifyIncomingDamage(PlayerEntity instance, DamageSource source, float amount, Operation<Boolean> original) {
		if(instance instanceof ServerPlayerEntity serverMario && serverMario.cpa$getCPAData().isEnabled()) {
			float modifiedAmount = serverMario.cpa$getCPAData().getCharacter().modifyIncomingDamage(serverMario.cpa$getCPAData(), source, amount);
			return modifiedAmount > 0 && original.call(instance, source, modifiedAmount);
		}
		return original.call(instance, source, amount);
	}

	@WrapWithCondition(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;jump()V"))
	private boolean preventLivingEntityJump(LivingEntity instance) {
		return !cpa$getCPAData().doMarioTravel();
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void writeMarioDataToNBT(NbtCompound nbt, CallbackInfo ci) {
		NbtCompound persistentData = new NbtCompound();
		CPAPlayerData data = cpa$getCPAData();

		boolean enabled = data.isEnabled();
		persistentData.putBoolean(MarioNbtKeys.ENABLED, enabled);
		if(enabled) {
			persistentData.putString(MarioNbtKeys.CHARACTER, data.getCharacterID().toString());
			persistentData.putString(MarioNbtKeys.POWER_UP, data.getPowerUpID().toString());
		}

		if(MarioQuaMario.CONFIG.logNBTReadWrite()) MarioQuaMario.LOGGER.info("Writing player NBT:\nEnabled: {}\nCharacter: {}\nPower-up: {}",
				persistentData.getBoolean(MarioNbtKeys.ENABLED),
				persistentData.getString(MarioNbtKeys.CHARACTER),
				persistentData.getString(MarioNbtKeys.POWER_UP));

		nbt.put(MarioNbtKeys.DATA, persistentData);
	}

	@Override
	public boolean isInSneakingPose() {
		return switch(cpa$getCPAData().isEnabled() ? cpa$getCPAData().getAction().SNEAKING_RULE : SneakingRule.ALLOW) {
			case ALLOW, SLIP -> super.isInSneakingPose();
			case PROHIBIT -> false;
			case FORCE -> true;
		};
	}

	@Override
	public void setSwimming(boolean swimming) {
		super.setSwimming(swimming && !this.cpa$getCPAData().isEnabled());
	}

	@Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
	private void prohibitDiveSwimming(CallbackInfo ci) {
		if(this.cpa$getCPAData().isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "isPartVisible", at = @At("HEAD"), cancellable = true)
	private void ensureCapeVisibility(PlayerModelPart modelPart, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@Inject(method = "isBlockBreakingRestricted", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"), cancellable = true)
	private void optionallyAllowBreakingBrittleBlocks(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
		if(MarioGamerules.adventurePlayersBreakBrittleBlocks) {
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(world);
			if(worldBaps != null && worldBaps.BRITTLE.contains(pos))
				cir.setReturnValue(false);
		}
	}

	@Override
	public void updateLimbs(boolean flutter) {
		if(!this.cpa$getCPAData().isEnabled()) {
			super.updateLimbs(flutter);
			return;
		}

		Vec3d fluidMotionVector = this.cpa$getCPAData().getFluidPushingVel();
		this.prevX -= fluidMotionVector.x;
		this.prevY -= fluidMotionVector.y;
		this.prevZ -= fluidMotionVector.z;
		super.updateLimbs(flutter);
		this.prevX += fluidMotionVector.x;
		this.prevY += fluidMotionVector.y;
		this.prevZ += fluidMotionVector.z;
	}

	@Inject(method = "getMaxRelativeHeadRotation", at = @At("HEAD"), cancellable = true)
	private void restrictHeadRotation(CallbackInfoReturnable<Float> cir) {
		CPAPlayerData data = this.cpa$getCPAData();
		if(data.isEnabled()) {
			if(data.headRestricted == CPAPlayerData.HeadRestrictionType.URGENT) {
				cir.setReturnValue(0F);
				data.headRestricted = CPAPlayerData.HeadRestrictionType.NONE;
			}
			else if(data.getActionCategory() == ActionCategory.WALLBOUND && ((ParsedWallboundAction) data.getAction()).ALIGNMENT != WallBodyAlignment.ANY)
				cir.setReturnValue(Float.MAX_VALUE); // Prevent head rotation from affecting body rotation
		}
	}

	@Override
	protected float turnHead(float bodyRotation, float headRotation) {
		@NotNull CPAPlayerData data = this.cpa$getCPAData();
		boolean rotateBody;

		if(data.isEnabled()) {
			if(data.headRestricted == CPAPlayerData.HeadRestrictionType.NORMAL) {
				if(MathHelper.abs((bodyRotation % 360) - (this.getYaw() % 360)) <= 10)
					data.headRestricted = CPAPlayerData.HeadRestrictionType.NONE;
				bodyRotation = this.getYaw();
			}

			rotateBody = data.getActionCategory() != ActionCategory.WALLBOUND || ((ParsedWallboundAction) data.getAction()).ALIGNMENT == WallBodyAlignment.ANY;
		}
		else rotateBody = true;

		return super.turnHead(rotateBody ? bodyRotation : this.bodyYaw, headRotation);
	}

	@Override
	public boolean isPushable() {
		return !this.cpa$getCPAData().isEnabled();
	}

	@Override
	public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
		super.changeLookDirection(cursorDeltaX, cursorDeltaY);
		if(this.cpa$getCPAData().isEnabled()) this.cpa$getCPAData().onMarioLookAround();
	}

	@Unique private static final int SPRINTING_FLAG_INDEX = 3;
}
