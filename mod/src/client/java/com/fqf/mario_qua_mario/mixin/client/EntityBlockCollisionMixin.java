package com.fqf.mario_qua_mario.mixin.client;

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

@Mixin(Entity.class)
public abstract class EntityBlockCollisionMixin {
	@Shadow private static List<VoxelShape> findCollisionsForMovement(@Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox) {
		return null;
	}

	// Predict which blocks Mario is about to collide with and bap them.
	// This is MASSIVELY easier than hooking directly into the collision logic and probably doesn't cost too much in
	// performance since it only runs on the main client player entity.
	// This will also hopefully make it so the BUST bap result will work super easily by just smashing the blocks before
	// movement-collision even occurs :)
	// TODO: Consider moving into MainClientPlayerEntity? Would need to duplicate or AW findCollisionsForMovement though :/
	@Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"))
	private void preemptiveBapCheck(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
		if((Entity) (Object) this instanceof ClientPlayerEntity mario) {
			boolean shouldRecalculate;
			do {
				shouldRecalculate = preemptMovementAndOptionallyRecalculate(mario, movement);
			}
			while(shouldRecalculate);
		}
	}

	@Unique
	private static boolean preemptMovementAndOptionallyRecalculate(ClientPlayerEntity mario, Vec3d movement) {
		MarioMainClientData data = mario.mqm$getMarioData();
		if(!data.isEnabled()) return false;

		int floorStrength = data.getBapStrength(Direction.DOWN);
		int ceilingStrength = data.getBapStrength(Direction.UP);
		int wallStrength = data.getBapStrength(Direction.NORTH);

		boolean bapFloors = floorStrength > 0;
		boolean bapCeilings = ceilingStrength > 0;
		double wallSpeedThreshold;
		if(wallStrength > 0) wallSpeedThreshold = data.getAction().BAPPING_RULE.wallBumpSpeedThreshold().getAsThreshold(data);
		else wallSpeedThreshold = 0.5;
		boolean bapWalls = wallStrength > 0 && movement.horizontalLengthSquared() > wallSpeedThreshold * wallSpeedThreshold;
		boolean bapOnXAxis = bapWalls && Math.abs(movement.x) > wallSpeedThreshold * 0.5;
		boolean bapOnZAxis = bapWalls && Math.abs(movement.z) > wallSpeedThreshold * 0.5;

		boolean requireCollisionRecording = false;

		if(!bapFloors && !bapCeilings && !bapOnXAxis && !bapOnZAxis && !requireCollisionRecording) return false;

		Box movedBox = mario.getBoundingBox();

		boolean movingUp = movement.y > 0;
		movedBox = bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.Y, (movingUp ? ceilingStrength : floorStrength) == 0 && !requireCollisionRecording);
		if(movedBox == null) return true;

		boolean doXFirst = Math.abs(movement.x) > Math.abs(movement.z);
		if(doXFirst) {
			movedBox = bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.X, !bapOnXAxis && !requireCollisionRecording);
			if(movedBox == null) return true;
		}
		movedBox = bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.Z, !bapOnZAxis && !requireCollisionRecording);
		if(movedBox == null) return true;
		if(!doXFirst) {
			movedBox = bapAlongAxis(mario, data, movedBox, movement, Direction.Axis.X, !bapOnXAxis && !requireCollisionRecording);
			return movedBox == null;
		}
		return false;
	}

	@Unique
	private static Box bapAlongAxis(ClientPlayerEntity mario, MarioMainClientData data, Box box, Vec3d movement, Direction.Axis axis, boolean shortcut) {
		double motion = movement.getComponentAlongAxis(axis);
		if(Math.abs(motion) < 1.0E-7) return box;

		Box stretchedBox = box.stretch(Vec3d.ZERO.withAxis(axis, motion));

		if(shortcut) {
			List<VoxelShape> list = findCollisionsForMovement(mario, mario.getWorld(), List.of(), stretchedBox);
			assert list != null;
			return box.offset(Vec3d.ZERO.withAxis(axis, VoxelShapes.calculateMaxOffset(axis, box, list, motion)));
		}

		Direction dir = Direction.from(axis, motion > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);

		Iterable<VoxelShapeWithBlockPos> collideBlocks = getBlockCollisionsWithPositions( mario.getWorld(), mario, stretchedBox);

		Set<BlockPos> collideWithBlockPositions = new HashSet<>();
		double absSmallestOffsetFound = Math.abs(motion);

		Box bumpingBox;
		if(axis.isHorizontal() && mario.isOnGround()) bumpingBox = box.withMinY(box.minY + mario.getStepHeight());
		else bumpingBox = box;

		for(VoxelShapeWithBlockPos positionedShape : collideBlocks) {
			double maxDist = positionedShape.shape().calculateMaxDistance(axis, bumpingBox, motion);

			if(Math.abs(maxDist) == absSmallestOffsetFound && maxDist != motion) collideWithBlockPositions.add(positionedShape.pos());
			else if(Math.abs(maxDist) < absSmallestOffsetFound) {
				collideWithBlockPositions.clear();
				collideWithBlockPositions.add(positionedShape.pos());
				absSmallestOffsetFound = Math.abs(maxDist);
			}
		}

		if(!collideWithBlockPositions.isEmpty()) {
			for(BlockPos pos : collideWithBlockPositions) {
				if(data.collideWithBlockAndOptionallyRecalculate(pos, dir))
					return null; // Force recalculation from the beginning
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
