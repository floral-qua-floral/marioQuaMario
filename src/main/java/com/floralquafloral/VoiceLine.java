package com.floralquafloral;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.util.ClientSoundPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Locale;
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
			Identifier id = Identifier.of(character.ID.getNamespace(), "voice." + character.ID.getPath() + "." + this.name().toLowerCase(Locale.ROOT));
			MarioQuaMario.LOGGER.info("Automatically registering VoiceLine sound event {}...", id);
			SoundEvent event = SoundEvent.of(id);
			SOUND_EVENTS.put(character, event);
			Registry.register(Registries.SOUND_EVENT, id, event);
		}
	}

	public void play(MarioData data, long seed) {
		MarioQuaMario.LOGGER.info("UWU: {}", SOUND_EVENTS.get(data.getCharacter()));

		PlayerEntity mario = data.getMario();

		if(mario.getWorld().isClient) {
			ClientSoundPlayer.SOUND_MANAGER.stop(PLAYER_VOICE_LINES.get(mario));

//			PositionedSoundInstance voiceSound = new PositionedSoundInstance(
//					SOUND_EVENTS.get(data.getCharacter()),
//					SoundCategory.VOICE,
//					1.0F,
//					1.0F,
//					Random.create(seed),
//					data.getMario().getX(),
//					data.getMario().getY(),
//					data.getMario().getZ()
//			);
//			ClientSoundPlayer.SOUND_MANAGER.play(voiceSound);
			PLAYER_VOICE_LINES.put(mario, ClientSoundPlayer.playSound(
					SOUND_EVENTS.get(data.getCharacter()),
					SoundCategory.VOICE,
					mario,
					1.0F,
					1.0F,
					seed
			));
		}
		else {
			MarioPackets.sendPacketToTrackers((ServerPlayerEntity) data.getMario(), new PlayVoiceLineS2CPayload(data.getMario(), this, seed));
		}
	}

	public static void registerPackets() {
		PlayVoiceLineS2CPayload.register();
	}
	public static void registerPacketsClient() {
		PlayVoiceLineS2CPayload.registerReceiver();
	}

	private record PlayVoiceLineS2CPayload(int player, int voiceLineOrdinal, long seed) implements CustomPayload {
		public static final Id<PlayVoiceLineS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "play_voice_line"));
		public static final PacketCodec<RegistryByteBuf, PlayVoiceLineS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, PlayVoiceLineS2CPayload::player,
				PacketCodecs.INTEGER, PlayVoiceLineS2CPayload::voiceLineOrdinal,
				PacketCodecs.VAR_LONG, PlayVoiceLineS2CPayload::seed,
				PlayVoiceLineS2CPayload::new
		);
		public PlayVoiceLineS2CPayload(PlayerEntity player, VoiceLine voiceLine, long seed) {
			this(player.getId(), voiceLine.ordinal(), seed);
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {

			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
