package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.actions.*;
import com.fqf.mario_qua_mario.definitions.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RegistryManager {
	public static void registerAll() {
		registerStompTypes();
		registerActions();
		registerPowerUps();
		registerCharacters();


		Registry.register(STOMP_TYPES, MarioQuaMario.makeID("stomp"), "BOING!");
		Registry.register(STOMP_TYPES, MarioQuaMario.makeID("ground_pound"), "POW!");

//		Registry.register(ACTIONS, MarioQuaMario.makeID("stomp"), null);
//		Registry.register(ACTIONS, MarioQuaMario.makeID("ground_pound"), null);

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

	public static final RegistryKey<Registry<AbstractParsedAction>> ACTIONS_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("actions"));
	public static final Registry<AbstractParsedAction> ACTIONS = FabricRegistryBuilder.createSimple(ACTIONS_KEY)
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

	public static <Definition> List<Definition> getEntrypoints(String key, Class<Definition> clazz) {
		return FabricLoader.getInstance().getEntrypointContainers(key, clazz).stream().map(EntrypointContainer::getEntrypoint).toList();
	}

	public static <Thing extends ParsedMarioThing> void registerThing(Registry<Thing> registry, Thing thing) {
		Registry.register(registry, thing.ID, thing);
	}

	private static void registerStompTypes() {

	}

	private static void registerActions() {
		List<IncompleteActionDefinition> actionDefinitions = new ArrayList<>();
		actionDefinitions.addAll(getEntrypoints("mqm-generic-actions", GenericActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("mqm-grounded-actions", GroundedActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("mqm-airborne-actions", AirborneActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("mqm-aquatic-actions", AquaticActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("mqm-wallbound-actions", WallboundActionDefinition.class));

		HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections = new HashMap<>();
		for(IncompleteActionDefinition definition : actionDefinitions) {
			registerThing(ACTIONS, ParsedActionHelper.parseAction(definition, allInjections));
		}

		// Now all actions are registered; we need to parse their transitions
		for(AbstractParsedAction action : ACTIONS) {
			action.parseTransitions(allInjections);
		}
	}

	private static void registerPowerUps() {

	}

	private static void registerCharacters() {

	}
}
