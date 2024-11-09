package com.floralquafloral.mariodata;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.util.JumpSoundPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public interface MarioClientSideData extends MarioData {
	SoundManager SOUND_MANAGER = MinecraftClient.getInstance().getSoundManager();

	default PositionedSoundInstance playSoundEvent(
			SoundEvent event, SoundCategory category,
			double x, double y, double z,
			float pitch, float volume, long seed
	) {
		PositionedSoundInstance sound = new PositionedSoundInstance(
				event, category,
				volume, pitch,
				Random.create(seed),
				x, y, z
		);
		SOUND_MANAGER.play(sound);
		return sound;
	}

	default PositionedSoundInstance playSoundEvent(SoundEvent event, long seed) {
		PlayerEntity mario = this.getMario();
		return playSoundEvent(
				event, SoundCategory.PLAYERS,
				mario.getX(), mario.getY(), mario.getZ(),
				1.0F, 1.0F, seed
		);
	}

	default PositionedSoundInstance playSoundEvent(SoundEvent event, float pitch, float volume, long seed) {
		PlayerEntity mario = this.getMario();
		return playSoundEvent(
				event, SoundCategory.PLAYERS,
				mario.getX(), mario.getY(), mario.getZ(),
				pitch, volume, seed
		);
	}

	default PositionedSoundInstance playSoundEvent(SoundEvent event, Entity entity, SoundCategory category, long seed) {
		return playSoundEvent(
				event, category,
				entity.getX(), entity.getY(), entity.getZ(),
				1.0F, 1.0F, seed
		);
	}

	default PositionedSoundInstance voice(VoiceLine line, long seed) {
		PlayerEntity mario = this.getMario();

		SOUND_MANAGER.stop(VoiceLine.MARIO_VOICE_LINES.get(this));

		PositionedSoundInstance newSoundInstance = this.playSoundEvent(
				line.SOUND_EVENTS.get(this.getCharacter()), SoundCategory.VOICE,
				mario.getX(), mario.getY(), mario.getZ(),
				1.0F, this.getPowerUp().VOICE_PITCH,
				seed
		);
		VoiceLine.MARIO_VOICE_LINES.put(this, newSoundInstance);

		return newSoundInstance;
	}

	default void playJumpSound(long seed) {
		JumpSoundPlayer.playJumpSfx(this, seed);
	}

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

		private static final Map<MarioClientSideData, PositionedSoundInstance> MARIO_VOICE_LINES = new HashMap<>();
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
	}
}
