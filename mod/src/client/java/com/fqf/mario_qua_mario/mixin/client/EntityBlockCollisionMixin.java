package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.util.VoxelShapeWithBlockPos;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(Entity.class) // FIXME Not registered, seems like an infeasible implementation; remove after first commit that includes it
public abstract class EntityBlockCollisionMixin {
//	@WrapMethod(method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;")
//	private static Vec3d marioBlockCollision(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions, Operation<Vec3d> original) {
//		if(entity instanceof PlayerEntity mario) {
//			MarioQuaMario.LOGGER.info("adjustMovementForCollisions Mario on {}!", mario.getWorld().isClient() ? "CLIENT" : "SERVER");
//		}
////		MarioQuaMario.LOGGER.info("adjustMovementForCollisions on {}", entity);
//
//		return original.call(entity, movement, entityBoundingBox, world, collisions);
//	}

	@Shadow private static List<VoxelShape> findCollisionsForMovement(@Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox) {
		return null;
	}

	@Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"))
	private void preemptiveBapCheck(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
		if((Entity) (Object) this instanceof ClientPlayerEntity mario) {
			MarioMainClientData data = mario.mqm$getMarioData();
			if(!data.isEnabled()) return;

			int floorStrength = 0; // TODO: Use actual bap strengths (should add to Action data first)
			int ceilingStrength = 10;
			int wallStrength = 10;

			boolean bapFloors = floorStrength > 0;
			boolean bapCeilings = ceilingStrength > 0;
			double wallSpeedThreshold;
			if(wallStrength > 0) wallSpeedThreshold = data.getAction().BUMP_TYPE.wallBumpSpeedThreshold().getAsThreshold(data);
			else wallSpeedThreshold = 0.5;
			boolean bapWalls = wallStrength > 0 && movement.horizontalLengthSquared() > wallSpeedThreshold * wallSpeedThreshold;
			boolean bapOnXAxis = bapWalls && Math.abs(movement.x) > wallSpeedThreshold * 0.5;
			boolean bapOnZAxis = bapWalls && Math.abs(movement.z) > wallSpeedThreshold * 0.5;

			if(!bapFloors && !bapCeilings && !bapOnXAxis && !bapOnZAxis) return;

			Box movedBox = mario.getBoundingBox();

			boolean movingUp = movement.y > 0;
			movedBox = bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.Y, movingUp ? ceilingStrength : floorStrength);

			boolean doXFirst = Math.abs(movement.x) > Math.abs(movement.z);
			if(doXFirst) movedBox = bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.X, bapOnXAxis ? wallStrength : 0);
			movedBox = bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.Z, bapOnZAxis ? wallStrength : 0);
			if(!doXFirst) bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.X, bapOnXAxis ? wallStrength : 0);
		}
	}

	@Unique
	private static Box bapAlongAxis(ClientPlayerEntity mario, MarioMainClientData data, Box box, Vec3d movement, Direction.Axis axis, int strength) {
		double motion = movement.getComponentAlongAxis(axis);
		if(Math.abs(motion) < 1.0E-7) return box;

		Box stretchedBox = box.stretch(Vec3d.ZERO.withAxis(axis, motion));

		if(strength <= 0) {
			List<VoxelShape> list = findCollisionsForMovement(mario, mario.getWorld(), List.of(), stretchedBox);
			assert list != null;
			return box.offset(Vec3d.ZERO.withAxis(axis, VoxelShapes.calculateMaxOffset(axis, box, list, motion)));
		}

		Direction dir = Direction.from(axis, motion > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);

		Iterable<VoxelShapeWithBlockPos> collideBlocks = getBlockCollisionsWithPositions( mario.getWorld(), mario, stretchedBox);

		Set<BlockPos> bapPositions = new HashSet<>();
		double absSmallestOffsetFound = Math.abs(motion);

		Box bumpingBox;
		if(axis.isHorizontal() && mario.isOnGround()) bumpingBox = box.withMinY(box.minY + mario.getStepHeight());
		else bumpingBox = box;

		for(VoxelShapeWithBlockPos positionedShape : collideBlocks) {
			double maxDist = positionedShape.shape().calculateMaxDistance(axis, bumpingBox, motion);

			if(Math.abs(maxDist) == absSmallestOffsetFound && maxDist != motion) bapPositions.add(positionedShape.pos());
			else if(Math.abs(maxDist) < absSmallestOffsetFound) {
				bapPositions.clear();
				bapPositions.add(positionedShape.pos());
				absSmallestOffsetFound = Math.abs(maxDist);
			}

			MarioQuaMario.LOGGER.info("Tested collision w/ {}:\n\tBlock: {}\n\t\tMax Offset: {}\n\t\tMovement: {}",
					positionedShape.pos(), mario.getWorld().getBlockState(positionedShape.pos()).getBlock(), maxDist, movement);
		}

		if(!bapPositions.isEmpty()) {
			MarioQuaMario.LOGGER.info("Selected baps w/ max offset {}", absSmallestOffsetFound);
			for(BlockPos pos : bapPositions) {
				BlockBappingUtil.attemptBap(data, mario.clientWorld, pos, dir, strength);
				MarioQuaMario.LOGGER.info("Bap {} @ ({}, {}, {}); dir={}, str={}",
						mario.getWorld().getBlockState(pos).getBlock().getName().toString(),
						pos.getX(), pos.getY(), pos.getZ(), dir, strength);
			}
		}

		return box.offset(Vec3d.ZERO.withAxis(axis, absSmallestOffsetFound * Math.signum(motion)));
	}

	@Unique
	private static Iterable<VoxelShapeWithBlockPos> getBlockCollisionsWithPositions(
			CollisionView world,
			ClientPlayerEntity mario,
			Box box
	) {
		return () -> new BlockCollisionSpliterator<>(world, mario, box, false, (pos, voxelShape) -> new VoxelShapeWithBlockPos(voxelShape, pos.toImmutable()));
	}
}
