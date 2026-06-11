package com.fqf.charaformact.mixin.player.functionality;

import com.fqf.charaformact.util.EntitiesMixinInterface;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixinForPlayers implements EntitiesMixinInterface {
	@WrapMethod(method = "stepOnBlock")
	private boolean preventStepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement, Operation<Boolean> original) {
		if(this.cfa$shouldStepOnBlock()) return original.call(pos, state, playSound, emitEvent, movement);
		else return false;
	}

	@WrapMethod(method = "playStepSounds")
	private void preventStepSounds(BlockPos pos, BlockState state, Operation<Void> original) {
		if(this.cfa$shouldStepOnBlock()) original.call(pos, state);
	}

	@WrapMethod(method = "calculateNextStepSoundDistance")
	private float calculateAppearanceBasedStepSoundDistance(Operation<Float> original) {
		return this.cfa$calculateNextStepSoundDistance(original);
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("RETURN"))
	private void enterMountedActionAfterMounting(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if(cir.getReturnValue()) this.cfa$afterMounting(entity);
	}

	@ModifyReturnValue(method = "isInSneakingPose", at = @At("RETURN"))
	private boolean isInSneakingPoseMixin(boolean original) {
		return this.cfa$isInSneakingPose(original);
	}

	@WrapMethod(method = "setSwimming")
	private void preventSwimming(boolean swimming, Operation<Void> original) {
		original.call(swimming && this.cfa$canSetSwimming());
	}

	@Inject(method = "changeLookDirection", at = @At("RETURN"))
	private void afterChangeLookDirectionMixin(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		this.cfa$afterChangeLookDirection();
	}
}
