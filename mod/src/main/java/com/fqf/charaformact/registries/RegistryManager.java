package com.fqf.charaformact.registries;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.CharaFormActAddon;
import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.ParsedActionHelper;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.util.CfaSounds;
import com.google.common.collect.ImmutableMap;
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
import java.util.function.BiConsumer;
import java.util.function.Function;

public class RegistryManager {
	public static void registerAll() {
		CfaSounds.staticInitialize();

		List<CharaFormActAddon> addons = getEntrypoints("charaformact-addon", CharaFormActAddon.class);

		registerCollisionAttackTypes(addons);
		registerActions(addons);
		registerForms(addons);
		registerCharacters(addons);

		registerVoicelines(addons);
	}

	public static final RegistryKey<Registry<ParsedCollisionAttack>> COLLISION_ATTACKS_KEY =
			RegistryKey.ofRegistry(CharaFormAct.makeID("collision_attacks"));
	public static final Registry<ParsedCollisionAttack> COLLISION_ATTACKS = FabricRegistryBuilder.createSimple(COLLISION_ATTACKS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<AbstractParsedAction>> ACTIONS_KEY =
			RegistryKey.ofRegistry(CharaFormAct.makeID("actions"));
	public static final Registry<AbstractParsedAction> ACTIONS = FabricRegistryBuilder.createSimple(ACTIONS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedForm>> FORMS_KEY =
			RegistryKey.ofRegistry(CharaFormAct.makeID("forms"));
	public static final Registry<ParsedForm> FORMS = FabricRegistryBuilder.createSimple(FORMS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedCharacter>> CHARACTERS_KEY =
			RegistryKey.ofRegistry(CharaFormAct.makeID("characters"));
	public static final Registry<ParsedCharacter> CHARACTERS = FabricRegistryBuilder.createSimple(CHARACTERS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<Map<ParsedCharacter, SoundEvent>>> VOICE_LINES_KEY =
			RegistryKey.ofRegistry(CharaFormAct.makeID("voicelines"));
	public static final Registry<Map<ParsedCharacter, SoundEvent>> VOICE_LINES = FabricRegistryBuilder.createSimple(VOICE_LINES_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();


	public static <Definition> List<Definition> getEntrypoints(String key, Class<Definition> clazz) {
		return FabricLoader.getInstance().getEntrypointContainers(key, clazz).stream().map(EntrypointContainer::getEntrypoint).toList();
	}

	private static <ThingDefinition, ParsedThing> void parseAndRegisterThings(
			Registry<ParsedThing> registry, List<CharaFormActAddon> addons,
			BiConsumer<CharaFormActAddon, ImmutableMap.Builder<Identifier, ThingDefinition>> accumulator,
			Function<ThingDefinition, ParsedThing> parser
	) {
		Map<Identifier, ThingDefinition> map = ImmutableCollectionHelper.accumulateMap(addons, accumulator);
		map.forEach((id, thingDefinition) -> Registry.register(registry, id, parser.apply(thingDefinition)));
		registry.freeze();
	}

	private static void registerCollisionAttackTypes(List<CharaFormActAddon> addons) {
		parseAndRegisterThings(
				COLLISION_ATTACKS, addons,
				CharaFormActAddon::accumulateCollisionAttackDefinitions,
				ParsedCollisionAttack::new
		);
	}

	private static int totalActionTransitions;
	public static void incrementTransitionCount() {
		totalActionTransitions++;
	}

	private static void registerActions(List<CharaFormActAddon> addons) {
		if(CharaFormAct.getClientHelper() != null) CharaFormAct.getClientHelper().prepareKeybindTexts();

		// Parse and register actions from every addon
		parseAndRegisterThings(ACTIONS, addons, CharaFormActAddon::accumulateActionDefinitions, ParsedActionHelper::parseAction);
		CharaFormAct.LOGGER.info("Registered {} actions...", ACTIONS.size());

		// Get all the transition injections provided by every addon
		List<TransitionInjectionDefinition> injections =
				ImmutableCollectionHelper.accumulateList(addons, CharaFormActAddon::accumulateTransitionInjectionDefinitions);
		CharaFormAct.LOGGER.info("Found {} total transition injection definitions...", injections.size());

		// Now all actions are registered and we have the transitions, so we can parse their transitions
		for(AbstractParsedAction action : ACTIONS) {
			action.parseTransitions(injections);
		}
		CharaFormAct.LOGGER.info("Parsed {} Action Transitions...", totalActionTransitions);

		// We can also register all Collision Attack Types' actions now. There's no reason to do this sooner since it uses a map; can't be final anyways
		for(ParsedCollisionAttack collisionAttackType : COLLISION_ATTACKS) {
			collisionAttackType.populatePostCollisionActions();
		}
	}

	private static void registerForms(List<CharaFormActAddon> addons) {
		parseAndRegisterThings(FORMS, addons, CharaFormActAddon::accumulateForms, ParsedForm::new);
	}

	private static void registerCharacters(List<CharaFormActAddon> addons) {
		parseAndRegisterThings(CHARACTERS, addons, CharaFormActAddon::accumulateCharacters, ParsedCharacter::new);
	}

	private static void registerVoicelines(List<CharaFormActAddon> addons) {
		for(Identifier voiceLine : ImmutableCollectionHelper.accumulateSet(addons, CharaFormActAddon::accumulateVoicelines)) {
			ImmutableMap.Builder<ParsedCharacter, SoundEvent> builder = ImmutableMap.builder();
			for(ParsedCharacter character : CHARACTERS) {
				Identifier eventID = Identifier.of(character.RESOURCE_ID.getNamespace(),
						"voice." + character.VOICE_NAME + "." + voiceLine.getPath());
				SoundEvent event = SoundEvent.of(eventID);
				Registry.register(Registries.SOUND_EVENT, eventID, event);
				builder.put(character, event);
			}
			Registry.register(VOICE_LINES, voiceLine, builder.build());
		}
	}
}
