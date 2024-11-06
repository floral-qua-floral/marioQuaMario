package com.floralquafloral.util;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.*;

import static com.floralquafloral.mariodata.MarioDataManager.getMarioData;

public class JumpSoundPlayer {
	public static void registerPackets() {
		FadeJumpSoundS2CPayload.register();
		FadeJumpSoundC2SPayload.register();

		FadeJumpSoundC2SPayload.registerReceiver();
	}
	public static void registerPacketsClient() {
		FadeJumpSoundS2CPayload.registerReceiver();
	}

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
		ClientSoundPlayer.SOUND_MANAGER.play(new JumpSoundInstance(event, mario, seed));
	}
	public static void playJumpSfx(MarioData data, long seed) {
		playJumpSfx(MarioSFX.JUMP, data, seed);
	}
	public static void fadeJumpSfx(PlayerEntity mario) {
		FADING_JUMPS.add(mario);
		if(mario.isMainPlayer()) ClientPlayNetworking.send(new FadeJumpSoundC2SPayload());
	}
	public static void fadeJumpSfx(MarioData data) {
		fadeJumpSfx(data.getMario());
	}

	private record FadeJumpSoundC2SPayload() implements CustomPayload {
		public static final Id<FadeJumpSoundC2SPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "fade_jump_c2s"));
		public static final PacketCodec<RegistryByteBuf, FadeJumpSoundC2SPayload> CODEC = PacketCodec.unit(new FadeJumpSoundC2SPayload());
		public static void registerReceiver() {
			ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					MarioPackets.sendPacketToTrackersExclusive(context.player(), new FadeJumpSoundS2CPayload(context.player()))
			);
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
		}
	}

	private record FadeJumpSoundS2CPayload(int player) implements CustomPayload {
		public static final Id<FadeJumpSoundS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "fade_jump_s2c"));
		public static final PacketCodec<RegistryByteBuf, FadeJumpSoundS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, FadeJumpSoundS2CPayload::player,
				FadeJumpSoundS2CPayload::new
		);
		public FadeJumpSoundS2CPayload(PlayerEntity player) {
			this(player.getId());
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					FADING_JUMPS.add(MarioPackets.getPlayerFromInt(context, payload.player)));
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
