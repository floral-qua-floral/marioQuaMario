package com.floralquafloral.registries;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioClientSideDataImplementation;
import com.floralquafloral.registries.states.action.ActionDefinition;
import com.floralquafloral.registries.states.action.AirborneActionDefinition;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.CharacterDefinition;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.floralquafloral.registries.states.powerup.PowerUpDefinition;
import com.floralquafloral.registries.stomp.ParsedStomp;
import com.floralquafloral.registries.stomp.StompDefinition;
import com.floralquafloral.util.MarioSFX;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.*;

public class RegistryManager {
	public static void register() {
		registerStomps();
		registerActions();
		registerPowerUps();
		registerCharacters();

		MarioSFX.staticInitialize();

		MarioClientSideDataImplementation.VoiceSoundEventInitializer.initialize();
	}

	public static final RegistryKey<Registry<ParsedStomp>> STOMP_TYPES_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "stomp_types"));
	public static final Registry<ParsedStomp> STOMP_TYPES = FabricRegistryBuilder.createSimple(STOMP_TYPES_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedAction>> ACTIONS_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "actions"));
	public static final Registry<ParsedAction> ACTIONS = FabricRegistryBuilder.createSimple(ACTIONS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedPowerUp>> POWER_UPS_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "power_ups"));
	public static final Registry<ParsedPowerUp> POWER_UPS = FabricRegistryBuilder.createSimple(POWER_UPS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedCharacter>> CHARACTERS_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "characters"));
	public static final Registry<ParsedCharacter> CHARACTERS = FabricRegistryBuilder.createSimple(CHARACTERS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static <T> List<T> getEntrypoints(String key, Class<T> clazz) {
		return FabricLoader.getInstance().getEntrypointContainers(key, clazz).stream().map(EntrypointContainer::getEntrypoint).toList();
	}

	private static void registerStomps() {
		for(StompDefinition definition : getEntrypoints("mario-stomp-types", StompDefinition.class)) {
			MarioQuaMario.LOGGER.info("Registering stomp type {}...", definition.getID());

			ParsedStomp stompType = new ParsedStomp(definition);
			Registry.register(STOMP_TYPES, stompType.ID, stompType);
		}
	}

	private static void parseAction(ActionDefinition definition, Map<Identifier, ArrayList<ActionDefinition.ActionTransitionInjection>> transitionInjections) {
		MarioQuaMario.LOGGER.info("Registering action {}...", definition.getID());

		ParsedAction action = new ParsedAction(definition);
		Registry.register(ACTIONS, action.ID, action);

		for(ActionDefinition.ActionTransitionInjection injection : definition.getTransitionInjections()) {
			transitionInjections.putIfAbsent(injection.INJECT_NEAR_TRANSITIONS_TO, new ArrayList<>());
			transitionInjections.get(injection.INJECT_NEAR_TRANSITIONS_TO).add(injection);
		}
	}

	/**
	 * Because actions are so interdependent and feature a great deal of complexity that individual Action
	 * implementations don't need to worry about, the definitions given by each entrypoint are parsed into a more
	 * efficient structure (the ParsedAction) before being registered.
	 * <p>
	 * Transitions are then also parsed for greater efficiency, to minimize registry calls needed for Action
	 * transitions (Transition definitions use the String ID of the target Action, while parsed transitions use a
	 * reference to the target ParsedAction itself). This process relies on every Action having alreayd been parsed and
	 * added to the registry, so it has to occur separately from Action registration.
	 */
	private static void registerActions() {
		MarioQuaMario.LOGGER.info("Registering actions...");

		Map<Identifier, ArrayList<ActionDefinition.ActionTransitionInjection>> transitionInjections = new HashMap<>();

		for(ActionDefinition definition : getEntrypoints("mario-actions-uncategorized", ActionDefinition.class)) {
			parseAction(definition, transitionInjections);
		}
		for(GroundedActionDefinition definition : getEntrypoints("mario-actions-grounded", GroundedActionDefinition.class)) {
			parseAction(definition, transitionInjections);
		}
		for(AirborneActionDefinition definition : getEntrypoints("mario-actions-airborne", AirborneActionDefinition.class)) {
			parseAction(definition, transitionInjections);
		}

		for(ParsedAction action : ACTIONS) {
			MarioQuaMario.LOGGER.info("Parsing and populating Action Transitions for {}...", action.ID);
			action.populateTransitionLists(transitionInjections);
		}
	}

	private static void registerPowerUps() {
		for(PowerUpDefinition definition : getEntrypoints("mario-power-ups", PowerUpDefinition.class)) {
			MarioQuaMario.LOGGER.info("Registering power-up {}...", definition.getID());

			ParsedPowerUp powerUp = new ParsedPowerUp(definition);
			Registry.register(POWER_UPS, powerUp.ID, powerUp);
		}
	}

	private static void registerCharacters() {
		for(CharacterDefinition definition : getEntrypoints("mario-characters", CharacterDefinition.class)) {
			MarioQuaMario.LOGGER.info("Registering character {}...", definition.getID());

			ParsedCharacter character = new ParsedCharacter(definition);
			Registry.register(CHARACTERS, character.ID, character);
		}
	}
}
