package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.registries.actions.ParsedAction;
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
		Registry.register(STOMP_TYPES, MarioQuaMario.makeID("stomp"), "BOING!");
		Registry.register(STOMP_TYPES, MarioQuaMario.makeID("ground_pound"), "POW!");

		Registry.register(POWER_UPS, MarioQuaMario.makeID("super"), "wahoo!");
		Registry.register(POWER_UPS, MarioQuaMario.makeID("small"), "owch!");

		Registry.register(CHARACTERS, MarioQuaMario.makeID("mario"), "yippee!");
		Registry.register(CHARACTERS, MarioQuaMario.makeID("toadette"), "nice!");
	}

	public static final RegistryKey<Registry<String>> STOMP_TYPES_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("stomp_types"));
	public static final Registry<String> STOMP_TYPES = FabricRegistryBuilder.createSimple(STOMP_TYPES_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedAction>> ACTIONS_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("actions"));
	public static final Registry<ParsedAction> ACTIONS = FabricRegistryBuilder.createSimple(ACTIONS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<String>> POWER_UPS_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("power_ups"));
	public static final Registry<String> POWER_UPS = FabricRegistryBuilder.createSimple(POWER_UPS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<String>> CHARACTERS_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("characters"));
	public static final Registry<String> CHARACTERS = FabricRegistryBuilder.createSimple(CHARACTERS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static <T> List<T> getEntrypoints(String key, Class<T> clazz) {
		return FabricLoader.getInstance().getEntrypointContainers(key, clazz).stream().map(EntrypointContainer::getEntrypoint).toList();
	}

	private static void registerStompTypes() {

	}

	private static void registerActions() {

	}

	private static void registerPowerUps() {

	}

	private static void registerCharacters() {

	}
}
