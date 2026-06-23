package com.fqf.charaformact.mixin.player.functionality;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.ParsedCommonAppearance;
import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.bapping.WorldBapsInfo;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.cfadata.injections.AdvCfaDataHolder;
import com.fqf.charaformact.registries.actions.parsed.ParsedWallboundAction;
import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact.util.EntitiesMixinInterface;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.SlidingStatus;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact.util.CfaNbtKeys;
import com.fqf.charaformact_api.definitions.states.actions.util.WallBodyAlignment;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements AdvCfaDataHolder, EntitiesMixinInterface {
	@Shadow public abstract EntityDimensions getBaseDimensions(EntityPose pose);

	@Shadow public float strideDistance;
	@Shadow public float prevStrideDistance;

	private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickHook(CallbackInfo ci) {
		CfaPlayerData data = this.cfa$getCfaData();
		if(data.isEnabled()) data.tick();
	}

	@Unique private boolean skippingThroughLivingEntityTravel;

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		if(this.cfa$getCfaData() instanceof CfaMoveableData moveableData
				&& moveableData.doCustomTravel(true)) {

			boolean cancelVanillaTravel = moveableData.actionPreventsVanillaTravel();
			moveableData.customTravel(cancelVanillaTravel, movementInput.z, movementInput.x);
			if(cancelVanillaTravel) {
				// SABLE COMPATIBILITY
				try {
					this.skippingThroughLivingEntityTravel = true;
					super.travel(movementInput);
				}
				finally {
					this.skippingThroughLivingEntityTravel = false;
				}

				ci.cancel();
			}
		}
	}

	@Override
	public boolean cfa$doLivingEntityTravel() {
		return !this.skippingThroughLivingEntityTravel;
	}


	@Override
	public boolean cfa$shouldStepOnBlock() {
		CfaPlayerData data = this.cfa$getCfaData();
		if(!data.isEnabled()) return true;
		return switch(data.getAction().SLIDING_STATUS) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> false;
			default -> true;
		};
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaSwimming(CallbackInfoReturnable<Boolean> cir) {
		if(cfa$getCfaData().doCustomTravel(false))
			cir.setReturnValue(false);
	}

	@Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaFluidPushing(CallbackInfoReturnable<Boolean> cir) {
		if(cfa$getCfaData().doCustomTravel(false))
			cir.setReturnValue(false);
	}

	@Inject(method = "getBaseDimensions", at = @At("RETURN"), cancellable = true)
	private void alterCharacterHitbox(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		CfaPlayerData data = cfa$getCfaData();
		if(data.isEnabled()) {
			// Return the standing hitbox if we're trying to evaluate the player's sneaking hitbox while she can't sneak
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
		CfaPlayerData data = cfa$getCfaData();
		if(data.doCustomTravel(false) && data.getAction().SNEAKING_RULE == SneakingRule.SLIP)
			cir.setReturnValue(false);
	}

	@Inject(method = "tickMovement", at = @At("TAIL"))
	private void preventViewBobbing(CallbackInfo ci) {
		CfaPlayerData data = this.cfa$getCfaData();
		if(data.isClient() && data.isEnabled() && data.getAction().SLIDING_STATUS != SlidingStatus.NOT_SLIDING)
			strideDistance = prevStrideDistance * 0.6F;
	}

	@Override
	public float cfa$calculateNextStepSoundDistance(Operation<Float> original) {
		ParsedCommonAppearance appearance = this.cfa$getCfaData().getAppearance();
		if(appearance == null) return EntitiesMixinInterface.super.cfa$calculateNextStepSoundDistance(original);
		else return this.distanceTraveled + appearance.STRIDE_LENGTH;
	}

	@Override
	public void cfa$afterMounting(Entity mount) {
		CfaPlayerData data = this.cfa$getCfaData();
		data.setActionTransitionless(data.getCharacter().getMountedAction(data, mount));
	}

	@Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
	private void changeDismounting(CallbackInfoReturnable<Boolean> cir) {
		CfaPlayerData data = cfa$getCfaData();
		if(data.isEnabled()) cir.setReturnValue(false);
	}

	@WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean modifyIncomingDamage(PlayerEntity instance, DamageSource source, float amount, Operation<Boolean> original) {
		if(instance instanceof ServerPlayerEntity serverPlayer && serverPlayer.cfa$getCfaData().isEnabled()) {
			float modifiedAmount = serverPlayer.cfa$getCfaData().getCharacter().modifyIncomingDamage(serverPlayer.cfa$getCfaData(), source, amount);
			return modifiedAmount > 0 && original.call(instance, source, modifiedAmount);
		}
		return original.call(instance, source, amount);
	}

	@WrapWithCondition(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;jump()V"))
	private boolean preventLivingEntityJump(LivingEntity instance) {
		return !cfa$getCfaData().doCustomTravel(false);
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void writeCfaDataToNBT(NbtCompound nbt, CallbackInfo ci) {
		NbtCompound persistentData = new NbtCompound();
		CfaPlayerData data = cfa$getCfaData();

		boolean enabled = data.isEnabled();
		persistentData.putBoolean(CfaNbtKeys.ENABLED, enabled);
		if(enabled) {
			persistentData.putString(CfaNbtKeys.CHARACTER, data.getCharacterID().toString());
			persistentData.putString(CfaNbtKeys.FORM, data.getFormID().toString());
		}

		if(CharaFormAct.CONFIG.logNBTReadWrite()) CharaFormAct.LOGGER.info("Writing player NBT:\nEnabled: {}\nCharacter: {}\nForm: {}",
				persistentData.getBoolean(CfaNbtKeys.ENABLED),
				persistentData.getString(CfaNbtKeys.CHARACTER),
				persistentData.getString(CfaNbtKeys.FORM));

		nbt.put(CfaNbtKeys.DATA, persistentData);
	}

	@Override
	public boolean cfa$isInSneakingPose(boolean vanillaResult) {
		return switch(cfa$getCfaData().isEnabled() ? cfa$getCfaData().getAction().SNEAKING_RULE : SneakingRule.ALLOW) {
			case ALLOW, SLIP -> vanillaResult;
			case PROHIBIT -> false;
			case FORCE -> true;
		};
	}

	@Override
	public boolean cfa$canSetSwimming() {
		return !this.cfa$getCfaData().isEnabled();
	}

	@Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
	private void prohibitDiveSwimming(CallbackInfo ci) {
		if(this.cfa$getCfaData().isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "isPartVisible", at = @At("HEAD"), cancellable = true)
	private void ensureCapeVisibility(PlayerModelPart modelPart, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@Inject(method = "isBlockBreakingRestricted", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"), cancellable = true)
	private void optionallyAllowBreakingBrittleBlocks(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
		if(CfaGamerules.adventurePlayersBreakBrittleBlocks) {
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(world);
			if(worldBaps != null && worldBaps.BRITTLE.contains(pos))
				cir.setReturnValue(false);
		}
	}

	@Inject(method = "getMaxRelativeHeadRotation", at = @At("HEAD"), cancellable = true)
	private void restrictHeadRotation(CallbackInfoReturnable<Float> cir) {
		CfaPlayerData data = this.cfa$getCfaData();
		if(data.isEnabled()) {
			if(data.headRestricted == CfaPlayerData.HeadRestrictionType.URGENT) {
				cir.setReturnValue(0F);
				data.headRestricted = CfaPlayerData.HeadRestrictionType.NONE;
			}
			else if(data.getActionCategory() == ActionCategory.WALLBOUND && ((ParsedWallboundAction) data.getAction()).ALIGNMENT != WallBodyAlignment.ANY)
				cir.setReturnValue(Float.MAX_VALUE); // Prevent head rotation from affecting body rotation
		}
	}

	@Override
	public float cfa$modifyBodyRotationForTurnHead(float bodyRotation) {
		CfaPlayerData data = this.cfa$getCfaData();
		boolean rotateBody;

		if(data.isEnabled()) {
			if(data.headRestricted == CfaPlayerData.HeadRestrictionType.NORMAL) {
				if(MathHelper.abs((bodyRotation % 360) - (this.getYaw() % 360)) <= 10)
					data.headRestricted = CfaPlayerData.HeadRestrictionType.NONE;
				bodyRotation = this.getYaw();
			}

			rotateBody = data.getActionCategory() != ActionCategory.WALLBOUND || ((ParsedWallboundAction) data.getAction()).ALIGNMENT == WallBodyAlignment.ANY;
		}
		else rotateBody = true;

		return rotateBody ? bodyRotation : this.bodyYaw;
	}

	@Override
	public void cfa$wrapPushAwayFrom(Entity entity, Operation<Void> original) {
		if(this.cfa$getCfaData() instanceof CfaMoveableData data && data.isEnabled()) {
			// Entity pushing affects special nudgeVel, which follows vanilla-like physics
			Vec3d trueVelocity = this.getVelocity();
			this.setVelocity(data.nudgeVel);
			original.call(entity);
			data.nudgeVel = this.getVelocity();
			this.setVelocity(trueVelocity);
			return;
		}
		original.call(entity);
	}

	@Override
	public void cfa$afterChangeLookDirection() {
		this.cfa$getCfaData().onLookAround();
	}
}
