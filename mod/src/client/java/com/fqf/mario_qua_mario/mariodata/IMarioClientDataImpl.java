package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public interface IMarioClientDataImpl extends IMarioClientData {
	@Override
	default boolean isClient() {
		return true;
	}

	@Override
	default SoundInstanceWrapperImpl playSound(
			SoundEvent event, SoundCategory category,
			double x, double y, double z,
			float pitch, float volume, long seed
	) {
		SoundInstance sound = new PositionedSoundInstance(
				event, category,
				volume, pitch,
				Random.create(seed),
				x, y, z
		);
		MinecraftClient.getInstance().getSoundManager().play(sound);
		return new SoundInstanceWrapperImpl(sound);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, long seed) {
		return this.playSound(event, 1F, 1F, seed);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, float pitch, float volume, long seed) {
		Vec3d marioPos = this.getMario().getPos();
		return this.playSound(event, SoundCategory.PLAYERS, marioPos.x, marioPos.y, marioPos.z, 1F, 1F, seed);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, Entity entity, SoundCategory category, long seed) {
		return this.playSound(event, category, entity.getX(), entity.getY(), entity.getZ(), 1F, 1F, seed);
	}

	@Override
	default void playJumpSound(long seed) {

	}

	@Override
	default void fadeJumpSound() {

	}

	Map<IMarioClientDataImpl, SoundInstance> MARIO_VOICE_LINES = new HashMap<>();

	@Override
	default SoundInstanceWrapperImpl voice(VoiceLine line, long seed) {
		MinecraftClient.getInstance().getSoundManager().stop(MARIO_VOICE_LINES.get(this));
		Vec3d marioPos = this.getMario().getPos();
		SoundInstanceWrapperImpl newVoiceSound = this.playSound(
				VOICE_SOUND_EVENTS.get(line).get(this.getCharacterID()), SoundCategory.VOICE,
				marioPos.x, marioPos.y, marioPos.z,
				this.getVoicePitch(), 1.0F,
				seed
		);

		MARIO_VOICE_LINES.put(this, newVoiceSound.SOUND);

		return newVoiceSound;
	}

	@Override
	default float getVoicePitch() {
//		return ((MarioPlayerData) this).getPowerUp().;
		return 1;
	}

	Map<IMarioClientDataImpl, Map<Identifier, SoundInstance>> STORED_SOUNDS = new HashMap<>();

	@Override
	default void storeSound(SoundInstanceWrapper instance) {
		STORED_SOUNDS.putIfAbsent(this, new HashMap<>());
		SoundInstance sound = ((SoundInstanceWrapperImpl) instance).SOUND;
		STORED_SOUNDS.get(this).put(sound.getId(), sound);
	}

	@Override
	default void stopStoredSound(SoundEvent event) {
		if(STORED_SOUNDS.containsKey(this)) {

		}
	}

	class SoundInstanceWrapperImpl implements SoundInstanceWrapper {
		private final SoundInstance SOUND;
		public SoundInstanceWrapperImpl(SoundInstance sound) {
			this.SOUND = sound;
		}
	}

	EnumMap<VoiceLine, Map<Identifier, SoundEvent>> VOICE_SOUND_EVENTS = new EnumMap<>(VoiceLine.class);
}
