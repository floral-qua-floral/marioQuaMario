package com.fqf.mario_qua_mario.util;

import it.unimi.dsi.fastutil.objects.ObjectDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

public class BlockCollisionFinder {
	public static Set<BlockPos> getCollidedBlockPositions(Entity mario, double motion, Direction.Axis axis) {
		return getCollidedBlockPositions(mario, mario.getBoundingBox(), motion, axis).left();
	}

	public static ObjectDoublePair<Set<BlockPos>> getCollidedBlockPositions(Entity mario, Box box, double motion, Direction.Axis axis) {
		Box stretchedBox = box.stretch(Vec3d.ZERO.withAxis(axis, motion));

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

		return new ObjectDoubleImmutablePair<>(collideWithBlockPositions, absSmallestOffsetFound * Math.signum(motion));
	}

	private static Iterable<VoxelShapeWithBlockPos> getBlockCollisionsWithPositions(
			CollisionView world,
			Entity mario,
			Box box
	) {
		return () -> new BlockCollisionSpliterator<>(world, mario, box, false, (pos, voxelShape) -> new VoxelShapeWithBlockPos(voxelShape, pos.toImmutable()));
	}

	private record VoxelShapeWithBlockPos(@NotNull VoxelShape shape, @Nullable BlockPos pos) {

	}
}
