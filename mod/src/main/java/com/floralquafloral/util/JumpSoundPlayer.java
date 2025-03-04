package com.floralquafloral.util;

import com.floralquafloral.mariodata.MarioData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

import java.util.*;

import static com.floralquafloral.mariodata.MarioDataManager.getMarioData;

public class JumpSoundPlayer {
	private static final Set<PlayerEntity> FADING_JUMPS = new HashSet<>();

	private static class JumpSoundInstance extends PositionedSoundInstance implements TickableSoundInstance {
		private final PlayerEntity owner;

		public JumpSoundInstance(SoundEvent event, PlayerEntity mario, long seed) {
			super(event, SoundCategory.PLAYERS, 1.0F, 1.0F, Random.create(seed), mario.getX(), mario.getY(), mario.getZ());
			owner = mario;
			FADING_JUMPS.remove(this.owner);
		}

		@Override
		public boolean isDone() {
			return volume <= 0.0F;
		}

		@Override
		public void tick() {
			if(FADING_JUMPS.contains(this.owner))
				this.volume -= 0.2F;
		}
	}

	public static void playJumpSfx(SoundEvent event, MarioData data, long seed) {
		PlayerEntity mario = data.getMario();
		MinecraftClient.getInstance().getSoundManager().play(new JumpSoundInstance(event, mario, seed));
	}
	public static void playJumpSfx(MarioData data, long seed) {
		playJumpSfx(MarioSFX.JUMP, data, seed);
	}
	public static void fadeJumpSfx(PlayerEntity mario) {
		FADING_JUMPS.add(mario);
	}
	public static void fadeJumpSfx(MarioData data) {
		fadeJumpSfx(data.getMario());
	}
}
