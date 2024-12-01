package com.floralquafloral.util;

import com.floralquafloral.mariodata.MarioData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;

public class DamageHelper {
	public static void damageEntity(
			MarioData data, float attackCooldownProgress,
			ServerWorld world, Entity target,
			RegistryKey<DamageType> damageType, float amount
	) {
		DamageSource source = new DamageSource(
				world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(damageType),
				data.getMario()
		);

		float progressFactor = 0.2F + attackCooldownProgress * attackCooldownProgress * 0.8F;

		target.damage(source, progressFactor * amount);
	}
}
