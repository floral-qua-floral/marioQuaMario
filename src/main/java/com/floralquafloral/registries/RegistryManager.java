package com.floralquafloral.registries;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.action.ActionDefinition;
import com.floralquafloral.registries.action.GroundedActionDefinition;
import com.floralquafloral.registries.action.ParsedAction;
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
import java.util.Map;

public class RegistryManager {
	public static final RegistryKey<Registry<ParsedAction>> ACTIONS_KEY = RegistryKey.ofRegistry(
			Identifier.of(MarioQuaMario.MOD_ID, "actions"));
	public static final Registry<ParsedAction> ACTIONS = FabricRegistryBuilder.createSimple(ACTIONS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static void register() {
		registerActions();
	}

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

	}

	private static void registerCharacters() {

	}
}
