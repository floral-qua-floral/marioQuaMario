package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SlidingStatus;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SneakingRule;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.AdvMarioDataHolder;
import com.fqf.mario_qua_mario.util.MarioNbtKeys;
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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements AdvMarioDataHolder {
	@Shadow public abstract EntityDimensions getBaseDimensions(EntityPose pose);

	@Shadow public float strideDistance;
	@Shadow public float prevStrideDistance;

	private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickHook(CallbackInfo ci) {
		MarioPlayerData data = this.mqm$getMarioData();
		if(data.isEnabled()) data.tick();
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		if(this.mqm$getMarioData() instanceof MarioMoveableData moveableData
				&& moveableData.doMarioTravel()
				&& moveableData.travelHook(movementInput.z, movementInput.x))
			ci.cancel();
	}

	@Override
	protected boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement) {
		return switch(this.mqm$getMarioData().isEnabled() ? this.mqm$getMarioData().getAction().SLIDING_STATUS : null) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> false;
			case null, default -> super.stepOnBlock(pos, state, playSound, emitEvent, movement);
		};
	}

	@Override
	protected void playStepSounds(BlockPos pos, BlockState state) {
		switch(this.mqm$getMarioData().isEnabled() ? this.mqm$getMarioData().getAction().SLIDING_STATUS : null) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> {}
			case null, default -> super.playStepSounds(pos, state);
		};
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaSwimming(CallbackInfoReturnable<Boolean> cir) {
		if(mqm$getMarioData().doMarioTravel())
			cir.setReturnValue(false);
	}

	@Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaFluidPushing(CallbackInfoReturnable<Boolean> cir) {
		if(mqm$getMarioData().doMarioTravel())
			cir.setReturnValue(false);
	}

	@Inject(method = "getBaseDimensions", at = @At("RETURN"), cancellable = true)
	private void alterMarioHitbox(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		MarioPlayerData data = mqm$getMarioData();
		if(data.isEnabled()) {
			// Return the standing hitbox if we're trying to evaluate Mario's sneaking hitbox while he can't sneak
			if(pose == EntityPose.CROUCHING && data.getAction().SNEAKING_RULE == SneakingRule.PROHIBIT)
				cir.setReturnValue(getBaseDimensions(EntityPose.STANDING));
			else if(pose == EntityPose.STANDING && data.getAction().SNEAKING_RULE == SneakingRule.FORCE)
				cir.setReturnValue(getBaseDimensions(EntityPose.CROUCHING));
			else {
				float widthFactor = data.getHorizontalScale();
				float heightFactor = data.getVerticalScale();
				if(pose == EntityPose.CROUCHING) heightFactor *= 0.6F;

				EntityDimensions normalDimensions = cir.getReturnValue();

				cir.setReturnValue(new EntityDimensions(
						normalDimensions.width() * widthFactor,
						normalDimensions.height() * heightFactor,
						normalDimensions.eyeHeight() * heightFactor,
						normalDimensions.attachments().scale(widthFactor, heightFactor, widthFactor), normalDimensions.fixed()
				));
			}
		}
	}

	@Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
	private void slideOffLedges(CallbackInfoReturnable<Boolean> cir) {
		MarioPlayerData data = mqm$getMarioData();
		if(data.doMarioTravel() && data.getAction().SNEAKING_RULE == SneakingRule.SLIP)
			cir.setReturnValue(false);
	}

	@Inject(method = "tickMovement", at = @At("TAIL"))
	private void preventViewBobbing(CallbackInfo ci) {
		MarioPlayerData data = mqm$getMarioData();
		if(data.isClient() && data.isEnabled() && data.getAction().SLIDING_STATUS != SlidingStatus.NOT_SLIDING)
			strideDistance = prevStrideDistance * 0.6F;
	}

	@Override
	public boolean startRiding(Entity entity, boolean force) {
		MarioQuaMario.LOGGER.info("Mounted on {}", (this.getWorld().isClient() ? "CLIENT" : "SERVER"));
		MarioPlayerData data = mqm$getMarioData();
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
		MarioPlayerData data = mqm$getMarioData();
		if(data.isEnabled()) cir.setReturnValue(false);
	}

//	@Override
//	protected void onDismounted(Entity vehicle) {
//		if(mqm$getMarioData().attemptDismount == MarioPlayerData.DismountType.DISMOUNT_IN_PLACE)
//			requestTeleportAndDismount(this.getX(), this.getY(), this.getZ());
//		else
//			super.onDismounted(vehicle);
//	}

	@WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean modifyIncomingDamage(PlayerEntity instance, DamageSource source, float amount, Operation<Boolean> original) {
		if(instance instanceof ServerPlayerEntity serverMario && serverMario.mqm$getMarioData().isEnabled()) {
			float modifiedAmount = serverMario.mqm$getMarioData().getCharacter().modifyIncomingDamage(serverMario.mqm$getMarioData(), source, amount);
			return modifiedAmount > 0 && original.call(instance, source, modifiedAmount);
		}
		return original.call(instance, source, amount);
	}

	@WrapWithCondition(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;jump()V"))
	private boolean preventLivingEntityJump(LivingEntity instance) {
		return !mqm$getMarioData().doMarioTravel();
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void writeMarioDataToNBT(NbtCompound nbt, CallbackInfo ci) {
		NbtCompound persistentData = new NbtCompound();
		MarioPlayerData data = mqm$getMarioData();

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
		return switch(mqm$getMarioData().isEnabled() ? mqm$getMarioData().getAction().SNEAKING_RULE : SneakingRule.ALLOW) {
			case ALLOW, SLIP -> super.isInSneakingPose();
			case PROHIBIT -> false;
			case FORCE -> true;
		};
	}

	@Override
	public void setSwimming(boolean swimming) {
		super.setSwimming(swimming && !this.mqm$getMarioData().isEnabled());
	}

	@Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
	private void prohibitDiveSwimming(CallbackInfo ci) {
		if(this.mqm$getMarioData().isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "isPartVisible", at = @At("HEAD"), cancellable = true)
	private void ensureCapeVisibility(PlayerModelPart modelPart, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@Override
	public void updateLimbs(boolean flutter) {
		if(!this.mqm$getMarioData().isEnabled()) {
			super.updateLimbs(flutter);
			return;
		}

		Vec3d fluidMotionVector = this.mqm$getMarioData().getFluidPushingVel();
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
		if(this.mqm$getMarioData().headRestricted == MarioPlayerData.HeadRestrictionType.URGENT) {
			cir.setReturnValue(0F);
			this.mqm$getMarioData().headRestricted = MarioPlayerData.HeadRestrictionType.NONE;
		}
	}

	@Override
	protected float turnHead(float bodyRotation, float headRotation) {
		if(this.mqm$getMarioData().headRestricted == MarioPlayerData.HeadRestrictionType.NORMAL) {
			if(MathHelper.abs(bodyRotation - this.getYaw()) <= 10)
				this.mqm$getMarioData().headRestricted = MarioPlayerData.HeadRestrictionType.NONE;
			bodyRotation = this.getYaw();
		}
		return super.turnHead(bodyRotation, headRotation);
	}

	@Override
	public boolean isPushable() {
		return !this.mqm$getMarioData().isEnabled();
	}
}
