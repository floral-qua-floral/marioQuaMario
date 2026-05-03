package com.fqf.charapoweract.registries;

import com.fqf.charapoweract.CharaPowerAct;
import com.fqf.charapoweract.registries.power_granting.ParsedPowerForm;
import com.fqf.charapoweract_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.charapoweract_api.definitions.VoicelineSetDefinition;
import com.fqf.charapoweract_api.definitions.states.CharacterDefinition;
import com.fqf.charapoweract_api.definitions.states.PowerFormDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.*;
import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charapoweract.registries.actions.AbstractParsedAction;
import com.fqf.charapoweract.registries.actions.ParsedActionHelper;
import com.fqf.charapoweract.registries.power_granting.ParsedCharacter;
import com.fqf.charapoweract.util.CPASounds;
import com.fqf.charapoweract.util.PlayermodelListener;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.*;

public class RegistryManager {
	public static void registerAll() {
		CPASounds.staticInitialize();

		registerCollisionAttackTypes();
		registerActions();
		registerPowerUps();
		registerCharacters();

		registerVoicelines();
	}

	public static final Map<String, Map<ParsedCharacter, SoundEvent>> VOICE_LINES = new HashMap<>();

	public static final RegistryKey<Registry<ParsedCollisionAttack>> COLLISION_ATTACKS_KEY =
			RegistryKey.ofRegistry(CharaPowerAct.makeID("collision_attacks"));
	public static final Registry<ParsedCollisionAttack> COLLISION_ATTACKS = FabricRegistryBuilder.createSimple(COLLISION_ATTACKS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<AbstractParsedAction>> ACTIONS_KEY =
			RegistryKey.ofRegistry(CharaPowerAct.makeID("actions"));
	public static final Registry<AbstractParsedAction> ACTIONS = FabricRegistryBuilder.createSimple(ACTIONS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedPowerForm>> POWER_FORMS_KEY =
			RegistryKey.ofRegistry(CharaPowerAct.makeID("power_forms"));
	public static final Registry<ParsedPowerForm> POWER_UPS = FabricRegistryBuilder.createSimple(POWER_FORMS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static final RegistryKey<Registry<ParsedCharacter>> CHARACTERS_KEY =
			RegistryKey.ofRegistry(CharaPowerAct.makeID("power_granting"));
	public static final Registry<ParsedCharacter> CHARACTERS = FabricRegistryBuilder.createSimple(CHARACTERS_KEY)
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	public static <Definition> List<Definition> getEntrypoints(String key, Class<Definition> clazz) {
		return FabricLoader.getInstance().getEntrypointContainers(key, clazz).stream().map(EntrypointContainer::getEntrypoint).toList();
	}

	public static <Thing extends ParsedCPAThing> void registerThing(Registry<Thing> registry, Thing thing) {
		if(registry.containsId(thing.ID))
			throw new IllegalStateException(thing.ID + " was registered twice as a " + thing.getClass().getName() + "!!!");
		Registry.register(registry, thing.ID, thing);
	}

	private static void registerCollisionAttackTypes() {
		for(CollisionAttackTypeDefinition definition : getEntrypoints("cpa-collision-attacks", CollisionAttackTypeDefinition.class)) {
			registerThing(COLLISION_ATTACKS, new ParsedCollisionAttack(definition));
		}
		COLLISION_ATTACKS.freeze();
	}

	private static int totalActionTransitions;
	public static void incrementTransitionCount() {
		totalActionTransitions++;
	}

	private static void registerActions() {
		List<IncompleteActionDefinition> actionDefinitions = new ArrayList<>();
		actionDefinitions.addAll(getEntrypoints("cpa-generic-actions", GenericActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("cpa-grounded-actions", GroundedActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("cpa-airborne-actions", AirborneActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("cpa-aquatic-actions", AquaticActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("cpa-wallbound-actions", WallboundActionDefinition.class));
		actionDefinitions.addAll(getEntrypoints("cpa-mounted-actions", MountedActionDefinition.class));

		HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections = new HashMap<>();
		for(IncompleteActionDefinition definition : actionDefinitions) {
			registerThing(ACTIONS, ParsedActionHelper.parseAction(definition, allInjections));
		}
		ACTIONS.freeze();

		// Now all actions are registered; we need to parse their transitions
		for(AbstractParsedAction action : ACTIONS) {
			action.parseTransitions(allInjections);
		}

		CharaPowerAct.LOGGER.info("Registered {} actions, with {} transitions connecting them.",
				ACTIONS.size(), totalActionTransitions);

		// We can also register all Collision Attack Types' actions now. There's no reason to do this sooner since it uses a map; can't be final anyways
		for(ParsedCollisionAttack collisionAttackType : COLLISION_ATTACKS) {
			collisionAttackType.populatePostCollisionActions();
		}
	}

	private static void registerPowerUps() {
		for(PowerFormDefinition definition : getEntrypoints("cpa-power-forms", PowerFormDefinition.class)) {
			registerThing(POWER_UPS, new ParsedPowerForm(definition));
		}
		POWER_UPS.freeze();
	}

	private static void registerCharacters() {
		Set<String> characterNamespaces = new HashSet<>();
		for(CharacterDefinition definition : getEntrypoints("cpa-characters", CharacterDefinition.class)) {
			ParsedCharacter character = new ParsedCharacter(definition);
			registerThing(CHARACTERS, character);
			characterNamespaces.add(character.RESOURCE_ID.getNamespace());
		}
		CHARACTERS.freeze();

		for(String namespace : characterNamespaces) {
			CharaPowerAct.LOGGER.info("Registering a playermodel resource listener for namespace \"{}\"...", namespace);
			ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PlayermodelListener(namespace));
		}
	}

	private static void registerVoicelines() {
		List<VoicelineSetDefinition> voicelineSetDefinitions = getEntrypoints("cpa-voicelines", VoicelineSetDefinition.class);
		for(VoicelineSetDefinition voicelineSet : voicelineSetDefinitions) {
			for (String voiceLine : voicelineSet.getVoiceLines()) {
				VOICE_LINES.put(voiceLine, new HashMap<>());
				for (ParsedCharacter character : CHARACTERS) {
					Identifier ID = Identifier.of(character.RESOURCE_ID.getNamespace(),
							"voice." + character.VOICE_NAME + "." + voiceLine);
					SoundEvent event = SoundEvent.of(ID);
					Registry.register(Registries.SOUND_EVENT, ID, event);
					VOICE_LINES.get(voiceLine).put(character, event);
				}
			}
		}
	}
}
