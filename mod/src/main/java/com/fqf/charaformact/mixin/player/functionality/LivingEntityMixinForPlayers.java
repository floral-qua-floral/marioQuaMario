package com.fqf.charaformact.mixin.player.functionality;

import com.fqf.charaformact.util.EntitiesMixinInterface;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixinForPlayers implements EntitiesMixinInterface {
	@WrapMethod(method = "turnHead")
	private float turnHeadMixin(float bodyRotation, float headRotation, Operation<Float> original) {
		return original.call(this.cfa$modifyBodyRotationForTurnHead(bodyRotation), headRotation);
	}

	@WrapMethod(method = "pushAwayFrom")
	private void pushAwayFromMixin(Entity entity, Operation<Void> original) {
		this.cfa$wrapPushAwayFrom(entity, original);
	}

	@WrapOperation(
			method = "updateLeaningPitch",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/LivingEntity;isInSwimmingPose()Z"
			)
	)
	private boolean preventSwimmingPoseLean(LivingEntity instance, Operation<Boolean> original) {
		return this.cfa$allowSwimmingPoseLean() && original.call(instance);
	}

	@WrapMethod(method = "isClimbing")
	private boolean overrideClimbingStatus(Operation<Boolean> original) {
		return this.cfa$isClimbing(original.call());
	}
}
