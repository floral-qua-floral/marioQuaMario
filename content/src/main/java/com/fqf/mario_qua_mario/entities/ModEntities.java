package com.fqf.mario_qua_mario.entities;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.entities.custom.MarioFireballProjectileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {
	public static final EntityType<MarioFireballProjectileEntity> MARIO_FIREBALL = Registry.register(
			Registries.ENTITY_TYPE,
			MarioQuaMarioContent.makeID("mario_fireball"),
			EntityType.Builder.<MarioFireballProjectileEntity>create(MarioFireballProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.5F, 0.5F).build()
	);
}
