package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
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
		MarioModSFX.staticInitialize();
		registerStompTypes();
		registerActions();
		registerPowerUps();
		registerCharacters();
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

	public static final RegistryKey<Registry<ParsedPowerUp>> POWER_UPS_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("powerups"));
	public static final Registry<ParsedPowerUp> POWER_UPS = FabricRegistryBuilder.createSimple(POWER_UPS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedCharacter>> CHARACTERS_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("power_granting"));
	public static final Registry<ParsedCharacter> CHARACTERS = FabricRegistryBuilder.createSimple(CHARACTERS_KEY)
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
		actionDefinitions.addAll(getEntrypoints("mqm-mounted-actions", MountedActionDefinition.class));

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
		for(PowerUpDefinition definition : getEntrypoints("mqm-power-ups", PowerUpDefinition.class)) {
			registerThing(POWER_UPS, new ParsedPowerUp(definition));
		}
	}

	private static void registerCharacters() {
		for(CharacterDefinition definition : getEntrypoints("mqm-characters", CharacterDefinition.class)) {
			registerThing(CHARACTERS, new ParsedCharacter(definition));
		}
	}
}
