package com.fqf.mario_qua_mario.item;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public interface MQMLootTables {
	RegistryKey<LootTable> POWER_UP_REDEMPTION = RegistryKey.of(RegistryKeys.LOOT_TABLE, MarioQuaMario.makeID("power_up_redemption"));

	static void staticInitialize() {

	}
}
