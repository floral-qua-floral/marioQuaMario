package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario_api.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario_api.definitions.VoicelineSetDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.PlayermodelListener;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.*;

public class RegistryManager {
	public static void registerAll() {
		MarioSFX.staticInitialize();

		registerStompTypes();
		registerActions();
		registerPowerUps();
		registerCharacters();

		registerVoicelines();
	}

	public static final Map<String, Map<ParsedCharacter, SoundEvent>> VOICE_LINES = new HashMap<>();

	public static final RegistryKey<Registry<ParsedStompType>> STOMP_TYPES_KEY =
			RegistryKey.ofRegistry(MarioQuaMario.makeID("stomp_types"));
	public static final Registry<ParsedStompType> STOMP_TYPES = FabricRegistryBuilder.createSimple(STOMP_TYPES_KEY)
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
		if(registry.containsId(thing.ID))
			throw new IllegalStateException(thing.ID + " was registered twice as a " + thing.getClass().getName() + "!!!");
		Registry.register(registry, thing.ID, thing);
	}

	private static void registerStompTypes() {
		for(StompTypeDefinition definition : getEntrypoints("mqm-stomp-types", StompTypeDefinition.class)) {
			registerThing(STOMP_TYPES, new ParsedStompType(definition));
		}
		STOMP_TYPES.freeze();
	}

	private static int totalActionTransitions;
	public static void incrementTransitionCount() {
		totalActionTransitions++;
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
		ACTIONS.freeze();

		// Now all actions are registered; we need to parse their transitions
		for(AbstractParsedAction action : ACTIONS) {
			action.parseTransitions(allInjections);
		}

		MarioQuaMario.LOGGER.info("Registered {} actions, with {} transitions connecting them.",
				ACTIONS.size(), totalActionTransitions);

		// We can also register all Stomp Types' actions now. There's no reason to do this sooner since it uses a map; can't be final anyways
		for(ParsedStompType stompType : STOMP_TYPES) {
			stompType.populatePostStompActions();
		}
	}

	private static void registerPowerUps() {
		for(PowerUpDefinition definition : getEntrypoints("mqm-power-ups", PowerUpDefinition.class)) {
			registerThing(POWER_UPS, new ParsedPowerUp(definition));
		}
		POWER_UPS.freeze();
	}

	private static void registerCharacters() {
		Set<String> characterNamespaces = new HashSet<>();
		for(CharacterDefinition definition : getEntrypoints("mqm-characters", CharacterDefinition.class)) {
			ParsedCharacter character = new ParsedCharacter(definition);
			registerThing(CHARACTERS, character);
			characterNamespaces.add(character.RESOURCE_ID.getNamespace());
		}
		CHARACTERS.freeze();

		for(String namespace : characterNamespaces) {
			MarioQuaMario.LOGGER.info("Registering a playermodel resource listener for namespace \"{}\"...", namespace);
			ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PlayermodelListener(namespace));
		}
	}

	private static void registerVoicelines() {
		List<VoicelineSetDefinition> voicelineSetDefinitions = getEntrypoints("mqm-voicelines", VoicelineSetDefinition.class);
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
