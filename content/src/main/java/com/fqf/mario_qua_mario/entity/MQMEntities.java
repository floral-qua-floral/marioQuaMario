package com.fqf.mario_qua_mario.entity;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.custom.BoatLikeTestEntity;
import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import com.fqf.mario_qua_mario.entity.custom.MarioFireballProjectileEntity;
import com.fqf.mario_qua_mario.entity.custom.MarioIceballProjectileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class MQMEntities {
	public static final EntityType<MarioFireballProjectileEntity> MARIO_FIREBALL = Registry.register(
			Registries.ENTITY_TYPE,
			MarioQuaMario.makeID("mario_fireball"),
			EntityType.Builder.<MarioFireballProjectileEntity>create(MarioFireballProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.5F, 0.5F).build()
	);

	public static final EntityType<MarioIceballProjectileEntity> MARIO_ICEBALL = Registry.register(
			Registries.ENTITY_TYPE,
			MarioQuaMario.makeID("mario_iceball"),
			EntityType.Builder.<MarioIceballProjectileEntity>create(MarioIceballProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.5F, 0.5F).build()
	);

	public static final EntityType<IceEncasementEntity> ICE_ENCASEMENT = Registry.register(
			Registries.ENTITY_TYPE,
			MarioQuaMario.makeID("ice_encasement"),
			EntityType.Builder.<IceEncasementEntity>create(IceEncasementEntity::new, SpawnGroup.MISC)
					.maxTrackingRange(32)
					.dimensions(1, 1)
					.trackingTickInterval(1)
					.build()
	);

	public static void registerModEntities() {

	}
}
