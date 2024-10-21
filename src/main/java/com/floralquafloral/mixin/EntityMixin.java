package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

	@Inject(method = "playStepSounds", at = @At("HEAD"), cancellable = true)
	private void preventStepSounds(BlockPos pos, BlockState state, CallbackInfo ci) {
		if(((Entity) (Object) this) instanceof PlayerEntity player) {
			MarioData data = MarioDataManager.getMarioData(player);
			if(!data.getAction().SLIDING_STATUS.doFootsteps())
				ci.cancel();
		}
	}

	@Unique
	private static boolean shouldStompHook = true;

	@Inject(method = "move", at = @At("HEAD"))
	private void executeStompsOnServer(MovementType movementType, Vec3d movement, CallbackInfo ci) {
		if((Entity) (Object) this instanceof ServerPlayerEntity player && shouldStompHook) {
			MarioData data = MarioDataManager.getMarioData(player);
			if(data.useMarioPhysics()) {
				ParsedAction action = data.getAction();
				if(action.STOMP != null) {
					shouldStompHook = false;
					action.STOMP.attempt(data, movement);
					shouldStompHook = true;
				}
			}
		}
	}
}
