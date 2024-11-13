package com.floralquafloral.mixin;

import com.floralquafloral.bumping.BumpManager;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockCollisionSpliterator;
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
//			MarioQuaMario.LOGGER.info("setPose called on player! Pose == {}", pose);
			if(MarioDataManager.getMarioData(player).getSneakProhibited()) {

				if(player.getPose() == EntityPose.CROUCHING)
					player.setPose(EntityPose.STANDING);
				ci.cancel();
			}
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

	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	private void executeStompsOnServer(MovementType movementType, Vec3d movement, CallbackInfo ci) {
		if((Entity) (Object) this instanceof ServerPlayerEntity mario && shouldStompHook) {
			MarioData data = MarioDataManager.getMarioData(mario);
			if(data.useMarioPhysics()) {
				ParsedAction action = data.getAction();
				if(action.STOMP != null) {
					shouldStompHook = false;
					if(action.STOMP.attempt((MarioServerData) data, movement)) ci.cancel();
					shouldStompHook = true;
				}
			}
		}
	}


//	@WrapOperation(method = "checkBlockCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
//	private void executeBumpsOnClient(BlockState instance, World world, BlockPos blockPos, Entity entity, Operation<Void> original) {
//		if(entity instanceof ServerPlayerEntity mario) {
////			world.breakBlock(blockPos, true, mario);
//		}
//	}

//	@Inject(method = "findCollisionsForMovement", at = @At("TAIL"))
//	private static void testasaur(@Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox, CallbackInfoReturnable<List<VoxelShape>> cir) {
//		if(entity instanceof ClientPlayerEntity clientPlayer) {
//			MarioQuaMario.LOGGER.info("findCollisionsForMovement on Mario!");
//			for(VoxelShape shape : cir.getReturnValue()) {
//				MarioQuaMario.LOGGER.info("Uwu: " + shape.);
//			}
//		}
//	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("HEAD"))
	private void setMountedAction(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if((Entity) (Object) this instanceof PlayerEntity mario) {
			MarioPlayerData data = MarioDataManager.getMarioData(mario);

			data.attemptDismount = false;
			data.setActionTransitionless(RegistryManager.ACTIONS.get(Identifier.of("qua_mario:mounted")));
		}
	}
}
