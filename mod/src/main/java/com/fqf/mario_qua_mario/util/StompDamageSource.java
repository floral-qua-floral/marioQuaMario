package com.fqf.mario_qua_mario.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class StompDamageSource extends DamageSource {
	public final float PIERCING;
	public final ItemStack WEAPON_STACK;

	public StompDamageSource(ServerWorld world, RegistryKey<DamageType> key, @Nullable Entity attacker, float piercing, ItemStack stompEquipment) {
		super(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
		this.PIERCING = piercing;
		this.WEAPON_STACK = stompEquipment;
	}

	@Override
	public @Nullable ItemStack getWeaponStack() {
		return this.WEAPON_STACK;
	}
}
