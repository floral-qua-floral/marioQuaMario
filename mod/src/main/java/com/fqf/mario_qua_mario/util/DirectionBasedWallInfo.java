package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.registries.actions.UniversalActionDefinitionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

public abstract class DirectionBasedWallInfo implements AdvancedWallInfo {
	public final MarioPlayerData OWNER;
	protected Direction wallDirection = Direction.NORTH;

	protected DirectionBasedWallInfo(MarioPlayerData owner) {
		OWNER = owner;
	}

	@Override
	public void setYaw(float yaw) {
		this.setDirection(Direction.fromRotation(yaw));
	}

	public void setDirection(Direction direction) {
		this.wallDirection = direction;
	}

	@Override
	public Vec3d getWallNormal() {
		return new Vec3d(this.wallDirection.getOpposite().getUnitVector());
	}

	@Override
	public float getWallYaw() {
		return switch(this.wallDirection) {
			case NORTH -> 180;
			case SOUTH -> 0;
			case WEST -> 90;
			case EAST -> -90;
			default -> throw new IllegalStateException("Illegal wall direction: " + this.wallDirection + " :(");
		};
	}

	private double getVelInDirection(Direction direction) {
		return this.OWNER.getVelocity().getComponentAlongAxis(direction.getAxis()) * direction.getDirection().offset();
	}

	@Override
	public double getTowardsWallVel() {
		return this.getVelInDirection(this.wallDirection);
	}

	@Override
	public double getSidleVel() {
		return this.getVelInDirection(this.wallDirection.rotateYClockwise());
	}

	@Override
	public float getYawDeviation() {
		return UniversalActionDefinitionHelper.INSTANCE.getAngleDifference(this.OWNER.getMario().getYaw(), this.getWallYaw());
	}

	@Override
	public double getDistanceFromWall(double maxDistance) {
		// This isn't very optimized(?) but I don't care that much TBH
		Direction.AxisDirection axisDir = this.wallDirection.getDirection();
		return Math.abs(Entity.adjustMovementForCollisions(
				this.OWNER.getMario(),
				Vec3d.ZERO.withAxis(this.wallDirection.getAxis(), maxDistance * axisDir.offset()),
				this.OWNER.getMario().getBoundingBox(),
				this.OWNER.getMario().getWorld(),
				List.of()
		).getComponentAlongAxis(this.wallDirection.getAxis()));
	}

	@Override
	public Set<BlockPos> getWallBlocks(double maxDistance) {
		return BlockCollisionFinder.getCollidedBlockPositions(this.OWNER.getMario(),
				maxDistance * this.wallDirection.getDirection().offset(), this.wallDirection.getAxis());
	}

	private void setDirectionVel(Direction direction, double velocity) {
		if(this.OWNER instanceof MarioMoveableData moveableOwner) {
			moveableOwner.setVelocity(this.OWNER.getVelocity().withAxis(direction.getAxis(),
					velocity * direction.getDirection().offset()));
		}
	}

	@Override
	public void setTowardsWallVel(double velocity) {
		this.setDirectionVel(this.wallDirection, velocity);
	}

	@Override
	public void setSidleVel(double velocity) {
		this.setDirectionVel(this.wallDirection.rotateYClockwise(), velocity);
	}
}
