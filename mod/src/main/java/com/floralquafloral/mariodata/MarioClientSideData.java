package com.floralquafloral.mariodata;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An instance of MarioData on the client-side. Mostly used for playing sound effects, especially voice sounds.
 * This isn't necessarily for the main client!
 */
public interface MarioClientSideData extends MarioData {
	PositionedSoundInstance playSoundEvent(
			SoundEvent event, SoundCategory category,
			double x, double y, double z,
			float pitch, float volume, long seed
	);

	PositionedSoundInstance playSoundEvent(SoundEvent event, long seed);
	PositionedSoundInstance playSoundEvent(SoundEvent event, float pitch, float volume, long seed);
	PositionedSoundInstance playSoundEvent(SoundEvent event, Entity entity, SoundCategory category, long seed);

	void playJumpSound(long seed);

	PositionedSoundInstance voice(VoiceLine line, long seed);

	enum VoiceLine {
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

		public static void staticInitialize() {

		}
		private final Map<ParsedCharacter, SoundEvent> SOUND_EVENTS;
		VoiceLine() {
			SOUND_EVENTS = new HashMap<>();

			for(ParsedCharacter character : RegistryManager.CHARACTERS) {
				Identifier id = Identifier.of(character.ID.getNamespace(), "voice." + character.ID.getPath() + "." + this.name().toLowerCase(Locale.ROOT));
				MarioQuaMario.LOGGER.info("Automatically registering VoiceLine sound event {}...", id);
				SoundEvent event = SoundEvent.of(id);
				Registry.register(Registries.SOUND_EVENT, id, event);
				SOUND_EVENTS.put(character, event);
			}
		}
		public SoundEvent getSoundEvent(ParsedCharacter character) {
			return SOUND_EVENTS.get(character);
		}
	}
}