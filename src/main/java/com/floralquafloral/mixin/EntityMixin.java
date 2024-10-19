package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.action.ActionDefinition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.floralquafloral.registries.action.ActionDefinition.IsSlidingOption.*;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(at = @At("HEAD"), method = "setSwimming(Z)V", cancellable = true)
	private void setSwimming(boolean swimming, CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		if(swimming && entity instanceof PlayerEntity player && MarioDataManager.getMarioData(player).isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "isInSneakingPose", cancellable = true)
	private void isInSneakingPose(CallbackInfoReturnable<Boolean> cir) {
		if((Entity) (Object) this instanceof PlayerEntity player) {
			if(MarioDataManager.getMarioData(player).getSneakProhibited())
				cir.setReturnValue(false);
		}
	}

	@Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
	private void preventSettingSneakPose(EntityPose pose, CallbackInfo ci) {
		if((Entity) (Object) this instanceof PlayerEntity player && pose == EntityPose.CROUCHING) {
			if(MarioDataManager.getMarioData(player).getSneakProhibited())
				ci.cancel();
		}
	}

	@Inject(method = "getPose", at = @At("TAIL"), cancellable = true)
	private void preventGettingSneakPose(CallbackInfoReturnable<EntityPose> cir) {
		if((Entity) (Object) this instanceof PlayerEntity player && cir.getReturnValue() == EntityPose.CROUCHING) {
			if(MarioDataManager.getMarioData(player).getSneakProhibited()) {
				player.setPose(EntityPose.STANDING);
				cir.setReturnValue(EntityPose.STANDING);
			}
		}
	}

	@Inject(method = "playStepSounds", at = @At("HEAD"), cancellable = true)
	private void preventStepSounds(BlockPos pos, BlockState state, CallbackInfo ci) {
		if(((Entity) (Object) this) instanceof PlayerEntity player) {
			MarioData data = MarioDataManager.getMarioData(player);
			ActionDefinition.IsSlidingOption isSliding = data.getAction().isSliding(data);
			if(isSliding != NOT_SLIDING && isSliding != NOT_SLIDING_SMOOTH)
				ci.cancel();
		}
	}
}
