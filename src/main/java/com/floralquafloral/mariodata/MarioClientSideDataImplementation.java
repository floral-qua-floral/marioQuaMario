package com.floralquafloral.mariodata;

import com.floralquafloral.util.JumpSoundPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;

/**
 * This is supposedly an interface but every method is implemented because that's the closest i can get to multiple
 * inheritance. endless pains and agonies for ever & ever!
 */
@SuppressWarnings("UnusedReturnValue")
public interface MarioClientSideDataImplementation extends MarioClientSideData {
	SoundManager SOUND_MANAGER = MinecraftClient.getInstance().getSoundManager();

	@Override
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

	@Override
	default PositionedSoundInstance playSoundEvent(SoundEvent event, long seed) {
		PlayerEntity mario = this.getMario();
		return playSoundEvent(
				event, SoundCategory.PLAYERS,
				mario.getX(), mario.getY(), mario.getZ(),
				1.0F, 1.0F, seed
		);
	}

	@Override
	default PositionedSoundInstance playSoundEvent(SoundEvent event, float pitch, float volume, long seed) {
		PlayerEntity mario = this.getMario();
		return playSoundEvent(
				event, SoundCategory.PLAYERS,
				mario.getX(), mario.getY(), mario.getZ(),
				pitch, volume, seed
		);
	}

	@Override
	default PositionedSoundInstance playSoundEvent(SoundEvent event, Entity entity, SoundCategory category, long seed) {
		return playSoundEvent(
				event, category,
				entity.getX(), entity.getY(), entity.getZ(),
				1.0F, 1.0F, seed
		);
	}

	Map<MarioClientSideDataImplementation, PositionedSoundInstance> MARIO_VOICE_LINES = new HashMap<>();

	@Override
	default PositionedSoundInstance voice(MarioClientSideData.VoiceLine line, long seed) {
		PlayerEntity mario = this.getMario();

		SOUND_MANAGER.stop(MARIO_VOICE_LINES.get(this));

		PositionedSoundInstance newSoundInstance = this.playSoundEvent(
				line.getSoundEvent(this.getCharacter()), SoundCategory.VOICE,
				mario.getX(), mario.getY(), mario.getZ(),
				this.getPowerUp().VOICE_PITCH, 1.0F,
				seed
		);
		MARIO_VOICE_LINES.put(this, newSoundInstance);

		return newSoundInstance;
	}

	@Override
	default void playJumpSound(long seed) {
		JumpSoundPlayer.playJumpSfx(this, seed);
	}

}
