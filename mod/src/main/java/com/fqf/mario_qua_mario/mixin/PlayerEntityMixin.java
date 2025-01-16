package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.actions.util.SlidingStatus;
import com.fqf.mario_qua_mario.definitions.states.actions.util.SneakingRule;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.MarioDataHolder;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements MarioDataHolder {
	@Shadow public abstract EntityDimensions getBaseDimensions(EntityPose pose);

	@Shadow public float strideDistance;
	@Shadow public float prevStrideDistance;

	private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickHook(CallbackInfo ci) {
		this.mqm$getMarioData().tick();
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
		return switch (this.mqm$getMarioData().getAction().SLIDING_STATUS) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> false;
			default -> super.stepOnBlock(pos, state, playSound, emitEvent, movement);
		};
	}

	@Override
	protected void playStepSounds(BlockPos pos, BlockState state) {
		switch (this.mqm$getMarioData().getAction().SLIDING_STATUS) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> {}
			default -> super.playStepSounds(pos, state);
		};
	}

	@WrapMethod(method = "damage")
	private boolean damageHook(DamageSource source, float amount, Operation<Boolean> original) {
		float factor;
		if(mqm$getMarioData().isEnabled()) factor = (float) getWorld().getGameRules().get(MarioGamerules.INCOMING_DAMAGE_MULTIPLIER).get();
		else factor = 1F;
		return original.call(source, amount * factor);
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaSwimming(CallbackInfoReturnable<Boolean> cir) {
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
			else {
				ParsedPowerUp powerUp = data.getPowerUp();
				ParsedCharacter character = data.getCharacter();

				float widthFactor = powerUp.WIDTH_FACTOR * character.WIDTH_FACTOR;
				float heightFactor = powerUp.HEIGHT_FACTOR * character.HEIGHT_FACTOR;
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
		if(data.isClient() && data.getAction().SLIDING_STATUS != SlidingStatus.NOT_SLIDING)
			strideDistance = prevStrideDistance * 0.6F;
	}

	@Override
	public boolean startRiding(Entity entity, boolean force) {
		MarioPlayerData data = mqm$getMarioData();
		boolean mounted = data.getCharacter().getMountedAction(entity) != null && super.startRiding(entity, force);
		if(mounted) {
			data.setActionTransitionless(data.getCharacter().getMountedAction(entity));
			data.attemptDismount = MarioPlayerData.DismountType.REMAIN_MOUNTED;
		}
		return mounted;
	}

	@Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
	private void changeDismounting(CallbackInfoReturnable<Boolean> cir) {
		MarioPlayerData data = mqm$getMarioData();
		if(data.isEnabled()) cir.setReturnValue(data.attemptDismount != MarioPlayerData.DismountType.REMAIN_MOUNTED);
	}

	@Override
	protected void onDismounted(Entity vehicle) {
		if(mqm$getMarioData().attemptDismount == MarioPlayerData.DismountType.DISMOUNT_IN_PLACE)
			requestTeleportAndDismount(this.getX(), this.getY(), this.getZ());
		else
			super.onDismounted(vehicle);
	}

	@WrapWithCondition(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;jump()V"))
	private boolean preventLivingEntityJump(LivingEntity instance) {
		return !mqm$getMarioData().doMarioTravel();
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);

		NbtCompound persistentData = new NbtCompound();
		MarioPlayerData data = mqm$getMarioData();

		persistentData.putBoolean("Enabled", data.isEnabled());
		persistentData.putString("PowerUp", data.getPowerUpID().toString());
		persistentData.putString("Character", data.getCharacterID().toString());

		MarioQuaMario.LOGGER.info("Wrote player NBT:\nEnabled: {}\nPower-up: {}\nCharacter: {}",
				persistentData.getBoolean("Enabled"),
				persistentData.getString("PowerUp"),
				persistentData.getString("Character"));

		nbt.put(MarioQuaMario.MOD_DATA_KEY, persistentData);
	}

	@Override
	public boolean isInSneakingPose() {
		return switch(mqm$getMarioData().getAction().SNEAKING_RULE) {
			case ALLOW, SLIP -> super.isInSneakingPose();
			case PROHIBIT -> false;
			case FORCE -> true;
		};
	}
}
