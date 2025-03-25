package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.util.Powers;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPlayerEntity.class)
public abstract class PlayerCeilingClippingMixin extends AbstractClientPlayerEntity {
	public PlayerCeilingClippingMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
		throw new AssertionError("Constructor on mixin?!?!");
	}

	@Unique
	public final double CLIPPING_LENIENCY = 0.33;

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		if(movementType == MovementType.SELF || movementType == MovementType.PLAYER) {
			if(movement.y > 0 && this.mqm$getIMarioData().hasPower(Powers.CEILING_CLIPPING)) {
				// If Mario's horizontal velocity is responsible for him clipping a ceiling, then just cancel his horizontal movement
				if(
						(movement.x != 0 || movement.z != 0)
								&& !getWorld().isSpaceEmpty(this, getBoundingBox().offset(movement)) // movement is blocked
								&& getWorld().isSpaceEmpty(this, getBoundingBox().offset(movement.x, 0, movement.z)) // can move straight ahead
								&& getWorld().isSpaceEmpty(this, getBoundingBox().offset(0, movement.y, 0)) // can move straight up
				) {
					movement = new Vec3d(0, movement.y, 0);
				}

				else if(!getWorld().isSpaceEmpty(this, getBoundingBox().offset(0, movement.y, 0))) {
					Box stretchedBox = getBoundingBox().stretch(0, movement.y, 0);
					if(getWorld().isSpaceEmpty(this, stretchedBox.offset(CLIPPING_LENIENCY, 0, 0))) {
						movement = new Vec3d(movement.x - CLIPPING_LENIENCY, movement.y, movement.z);
						move(MovementType.SELF, new Vec3d(CLIPPING_LENIENCY, 0, 0));
					}
					else if(getWorld().isSpaceEmpty(this, stretchedBox.offset(-CLIPPING_LENIENCY, 0, 0))) {
						movement = new Vec3d(movement.x + CLIPPING_LENIENCY, movement.y, movement.z);
						move(MovementType.SELF, new Vec3d(-CLIPPING_LENIENCY, 0, 0));
					}
					if(getWorld().isSpaceEmpty(this, stretchedBox.offset(0, 0, CLIPPING_LENIENCY))) {
						movement = new Vec3d(movement.x, movement.y, movement.z - CLIPPING_LENIENCY);
						move(MovementType.SELF, new Vec3d(0, 0, CLIPPING_LENIENCY));
					}
					else if(getWorld().isSpaceEmpty(this, stretchedBox.offset(0, 0, -CLIPPING_LENIENCY))) {
						movement = new Vec3d(movement.x, movement.y, movement.z + CLIPPING_LENIENCY);
						move(MovementType.SELF, new Vec3d(0, 0, -CLIPPING_LENIENCY));
					}
				}
			}
		}

		super.move(movementType, movement);
	}
}
