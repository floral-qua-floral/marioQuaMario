package com.fqf.mario_qua_mario.entity.custom;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractSolidMovingEntity extends Entity {
	public AbstractSolidMovingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public static final ThreadLocal<Boolean> solidFromAllSides = new ThreadLocal<>();
	static {
		solidFromAllSides.set(true);
	}
	private static double legalityCheckerFootY;
//	public static void runWithSuppressedMovingSolidCollisions(double footY, Runnable runnable) {
//		try {
//			solidFromAllSides = false;
//			legalityCheckerFootY = footY;
//			runnable.run();
//		}
//		finally {
//			solidFromAllSides = true;
//		}
//	}

	@Override
	public boolean isCollidable() {
		// This is hideous >:(
		if(this.getWorld().isClient) return true;
		Boolean solidFromAllSidesValue = solidFromAllSides.get();
		if(solidFromAllSidesValue == null) {
			solidFromAllSides.set(true);
			solidFromAllSidesValue = true;
		}
		return solidFromAllSidesValue;
	}

	@Override
	public boolean collidesWith(Entity other) {
		return BoatEntity.canCollide(this, other);
	}

	@Override
	public void tick() {
		super.tick();

		this.updateVelocity();
		if(this.getVelocity().lengthSquared() < 0.00001)
			this.setVelocity(Vec3d.ZERO);
		this.move(MovementType.SELF, this.getVelocity());
	}

	protected abstract void updateVelocity();

	@Override
	public float getLerpTargetPitch() {
		return 0;
	}

	@Override
	public float getLerpTargetYaw() {
		return 0;
	}

	private final Predicate<Entity> CAN_PUSH_WITH_MOVEMENT = EntityPredicates.EXCEPT_SPECTATOR
			.and(this::collidesWith)
			.and(other -> !(other instanceof AbstractSolidMovingEntity));
	private final Predicate<Entity> CAN_CARRY_WITH_MOVEMENT = CAN_PUSH_WITH_MOVEMENT
			.and(Entity::isOnGround);

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		if(movement.lengthSquared() == 0) {
			super.move(movementType, movement);
			return;
		}

		Box preMoveBoundingBox = this.getBoundingBox();

		// First, ensure that entities in the path of the ice block won't block its motion (unless that entity can't be
		// moved out of the way, of course)

		Direction.Axis mostSignificantAxis;
		{
			double xDistance = Math.abs(movement.x);
			double yDistance = Math.abs(movement.y);
			double zDistance = Math.abs(movement.z);
			if (xDistance > yDistance) {
				if (xDistance > zDistance) mostSignificantAxis = Direction.Axis.X;
				else mostSignificantAxis = Direction.Axis.Z;
			} else {
				if (yDistance > zDistance) mostSignificantAxis = Direction.Axis.Y;
				else mostSignificantAxis = Direction.Axis.Z;
			}
		}

		Box movementStretchedBox = preMoveBoundingBox.stretch(movement.multiply(1.5, 0, 1.5));
		List<Entity> inPathOfMotion = this.getWorld().getOtherEntities(this,
				movementStretchedBox, CAN_PUSH_WITH_MOVEMENT);
		if(!inPathOfMotion.isEmpty()) {
			/*
			 TODO: Make it so that the moving solid selects an axis to base its pushing on per-entity, based on which
			  faces are touching, rather than based on just the moving solid's motion. This will be necessary in order
			  to support pushing up and down.

			 ALSO TODO: Make moving solids actively shove entities out of their hitbox, even when not moving.
			*/

			boolean isPositive = movement.getComponentAlongAxis(mostSignificantAxis) > 0;

			double leadingEdge = isPositive
					? movementStretchedBox.getMax(mostSignificantAxis)
					: movementStretchedBox.getMin(mostSignificantAxis);

			inPathOfMotion.forEach(other -> {
				double targetCoord = leadingEdge + (mostSignificantAxis.isVertical()
						? isPositive ? 0 : -other.getHeight()
						: (isPositive ? 1 : -1) * other.getWidth() / 2
				);
				double offsetToTarget = targetCoord - other.getPos().getComponentAlongAxis(mostSignificantAxis);
				double proportion = offsetToTarget / movement.getComponentAlongAxis(mostSignificantAxis);

				Vec3d offsetMovement = movement.multiply(proportion, 0, proportion);
				if(other.isOnGround() && movement.y <= 0) offsetMovement = offsetMovement.add(0, -0.15, 0);

				other.move(MovementType.SHULKER, offsetMovement);

				if(movement.y > 0) other.setOnGround(true);
			});
		}

		// Now that obstructing entities are out of the way, do normal entity movement
		Vec3d preMovePos = this.getPos();
//		List<VoxelShape> shapesToCollide = this.getWorld().getEntityCollisions(this, this.getBoundingBox().stretch(movement));

		super.move(movementType, movement);

		// If we're moving down, then we need to move the entities standing on us now, for two reasons:
		// 1. They wouldn't have been captured by inPathOfMotion
		// 2. Even if they were, the ice block itself would have blocked the downwards movement regardless
		if(movement.y <= 0) {
			List<Entity> standingAtop = this.getWorld().getOtherEntities(this,
					preMoveBoundingBox.stretch(0, 0.1, 0), CAN_CARRY_WITH_MOVEMENT);
			standingAtop.removeAll(inPathOfMotion);
			if(!standingAtop.isEmpty()) {
				// Add a little downward motion so that we don't completely ruin the isOnGround status
				Vec3d deltaPos = this.getPos().subtract(preMovePos).add(0, -0.15, 0);
				standingAtop.forEach(other -> other.move(MovementType.SHULKER, deltaPos));
			}
		}
	}
}
