package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.entity.custom.AbstractSolidMovingEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public class LenientCollisionCheckMixin {
	@Shadow public ServerPlayerEntity player;

	@WrapOperation(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
	private void makeMovingSolidsOnlySolidFromAbove(ServerPlayerEntity instance, MovementType movementType, Vec3d vec3d, Operation<Void> original) {
		AbstractSolidMovingEntity.solidFromAllSides.set(false);
		original.call(instance, movementType, vec3d);
		AbstractSolidMovingEntity.solidFromAllSides.set(true);

//		AbstractSolidMovingEntity.runWithSuppressedMovingSolidCollisions(
//				this.player.getY(),
//				() -> original.call(packet)
//		);
	}

	@WrapOperation(
			method = "isPlayerNotCollidingWithBlocks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/WorldView;getCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/lang/Iterable;"
			)
	)
	private Iterable<VoxelShape> again(WorldView instance, Entity entity, Box box, Operation<Iterable<VoxelShape>> original) {
		AbstractSolidMovingEntity.solidFromAllSides.set(false);
		Iterable<VoxelShape> value = original.call(instance, entity, box);
		AbstractSolidMovingEntity.solidFromAllSides.set(true);
		return value;
	}
}
