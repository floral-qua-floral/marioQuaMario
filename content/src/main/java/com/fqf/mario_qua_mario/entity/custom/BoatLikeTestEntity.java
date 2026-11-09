package com.fqf.mario_qua_mario.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class BoatLikeTestEntity extends BoatEntity {
	public BoatLikeTestEntity(EntityType<? extends BoatEntity> entityType, World world) {
		super(entityType, world);
	}
}
