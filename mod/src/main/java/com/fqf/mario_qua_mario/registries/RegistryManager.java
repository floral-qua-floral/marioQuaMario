package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.List;

public class RegistryManager {
	public static void register() {
		Registry.register(STOMP_TYPES, Identifier.of(MarioQuaMario.MOD_ID, "stomp"), "BOING!");
		Registry.register(STOMP_TYPES, Identifier.of(MarioQuaMario.MOD_ID, "ground_pound"), "POW!");

		Registry.register(ACTIONS, Identifier.of(MarioQuaMario.MOD_ID, "debug"), "float...");
		Registry.register(ACTIONS, Identifier.of(MarioQuaMario.MOD_ID, "debug_alt"), "zoom!");

		Registry.register(POWER_UPS, Identifier.of(MarioQuaMario.MOD_ID, "super"), "wahoo!");
		Registry.register(POWER_UPS, Identifier.of(MarioQuaMario.MOD_ID, "small"), "owch!");

		Registry.register(CHARACTERS, Identifier.of(MarioQuaMario.MOD_ID, "mario"), "yippee!");
		Registry.register(CHARACTERS, Identifier.of(MarioQuaMario.MOD_ID, "toadette"), "nice!");
	}

	public static final RegistryKey<Registry<String>> STOMP_TYPES_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "stomp_types"));
	public static final Registry<String> STOMP_TYPES = FabricRegistryBuilder.createSimple(STOMP_TYPES_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<String>> ACTIONS_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "actions"));
	public static final Registry<String> ACTIONS = FabricRegistryBuilder.createSimple(ACTIONS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<String>> POWER_UPS_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "power_ups"));
	public static final Registry<String> POWER_UPS = FabricRegistryBuilder.createSimple(POWER_UPS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<String>> CHARACTERS_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "characters"));
	public static final Registry<String> CHARACTERS = FabricRegistryBuilder.createSimple(CHARACTERS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static <T> List<T> getEntrypoints(String key, Class<T> clazz) {
		return FabricLoader.getInstance().getEntrypointContainers(key, clazz).stream().map(EntrypointContainer::getEntrypoint).toList();
	}


}
