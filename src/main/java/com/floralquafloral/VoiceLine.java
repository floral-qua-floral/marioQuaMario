package com.floralquafloral;

import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public enum VoiceLine {
	SELECT,
	DUCK,

	DOUBLE_JUMP,
	TRIPLE_JUMP,
	GYMNAST_SALUTE,

	DUCK_JUMP,
	LONG_JUMP,
	BACKFLIP,
	SIDEFLIP,
	WALL_JUMP,

	REVERT,
	BURNT,

	FIREBALL,
	GET_STAR;

	private static final VoiceLine[] VOICE_LINE_VALUES = VoiceLine.values();
	private static final Map<PlayerEntity, PositionedSoundInstance> PLAYER_VOICE_LINES = new HashMap<>();
	private final Map<ParsedCharacter, SoundEvent> SOUND_EVENTS;

	VoiceLine() {
		SOUND_EVENTS = new HashMap<>();

		for(ParsedCharacter character : RegistryManager.CHARACTERS) {
			Identifier id = Identifier.of(character.ID.getNamespace(), "voice." + character.ID.getPath() + "." + this.name().toLowerCase());
			SoundEvent event = SoundEvent.of(id);
			SOUND_EVENTS.put(character, event);
			Registry.register(Registries.SOUND_EVENT, id, event);
		}
	}
}
