package com.floralquafloral.registries;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.action.ActionDefinition;
import com.floralquafloral.registries.action.GroundedActionDefinition;
import com.floralquafloral.registries.action.ParsedAction;
import com.floralquafloral.registries.character.CharacterDefinition;
import com.floralquafloral.registries.character.ParsedCharacter;
import com.floralquafloral.registries.powerup.ParsedPowerUp;
import com.floralquafloral.registries.powerup.PowerUpDefinition;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryManager {
	public static void register() {
		registerActions();
		registerPowerUps();
		registerCharacters();

		registerSounds();
	}

	public static final SoundEvent JUMP_SFX = makeAndRegisterSound("sfx.jump");

	public static final SoundEvent STOMP_SFX = makeAndRegisterSound("sfx.stomp");

	public static final SoundEvent POWER_UP_SFX = makeAndRegisterSound("sfx.power_up_wii");

	public static void registerSounds() {

	}

	private static SoundEvent makeAndRegisterSound(String id) {
		Identifier identifier = Identifier.of(MarioQuaMario.MOD_ID, id);
		SoundEvent event = SoundEvent.of(identifier);

		Registry.register(Registries.SOUND_EVENT, identifier, event);

		return event;
	}

	// STATE REGISTRIES:
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

	private static <T extends MarioStateDefinition> List<T> getEntrypoints(String key, Class<T> clazz) {
		return FabricLoader.getInstance().getEntrypointContainers(key, clazz).stream().map(EntrypointContainer::getEntrypoint).toList();
	}

	private static void parseAction(ActionDefinition definition, Map<Identifier, ArrayList<ActionDefinition.ActionTransitionDefinition>> transitionInjections) {
		MarioQuaMario.LOGGER.info("Registering action {}...", definition.getID());

		ParsedAction action = new ParsedAction(definition);
		Registry.register(ACTIONS, action.ID, action);

		for(ActionDefinition.ActionTransitionInjection injection : definition.getTransitionInjections()) {
			transitionInjections.putIfAbsent(injection.INJECT_BEFORE_TRANSITIONS_TO, new ArrayList<>());
			transitionInjections.get(injection.INJECT_BEFORE_TRANSITIONS_TO).add(injection.TRANSITION);
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

		Map<Identifier, ArrayList<ActionDefinition.ActionTransitionDefinition>> transitionInjections = new HashMap<>();

		for(ActionDefinition definition : getEntrypoints("mario-actions-uncategorized", ActionDefinition.class)) {
			parseAction(definition, transitionInjections);
		}
		for(GroundedActionDefinition definition : getEntrypoints("mario-actions-grounded", GroundedActionDefinition.class)) {
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
