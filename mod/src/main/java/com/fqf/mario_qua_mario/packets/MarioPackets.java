package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioPackets {
	public static void register() {
		MarioDataPackets.DisableMarioS2CPayload.register();

		MarioDataPackets.ActionTransitionS2CPayload.register();
		MarioDataPackets.AssignActionS2CPayload.register();

		MarioDataPackets.SetActionC2SPayload.register();

		MarioDataPackets.EmpowerRevertS2CPayload.register();
		MarioDataPackets.AssignPowerUpS2CPayload.register();

		MarioDataPackets.AssignCharacterS2CPayload.register();

		SyncUseCharacterStatsS2CPayload.register();

		MarioAttackInterceptionPackets.MissedAttackInterceptedC2SPayload.register();
		MarioAttackInterceptionPackets.EntityAttackInterceptedC2SPayload.register();
		MarioAttackInterceptionPackets.BlockAttackInterceptedC2SPayload.register();

		MarioAttackInterceptionPackets.MissedAttackInterceptedS2CPayload.register();
		MarioAttackInterceptionPackets.EntityAttackInterceptedS2CPayload.register();
		MarioAttackInterceptionPackets.BlockAttackInterceptedS2CPayload.register();
	}

	public static void syncUseCharacterStatsS2C(boolean shouldUse) {

	}

	public static void sendToTrackers(ServerPlayerEntity mario, CustomPayload packet, boolean includeMario) {
		if(includeMario) ServerPlayNetworking.send(mario, packet);
		for(ServerPlayerEntity player : PlayerLookup.tracking(mario)) {
			if(!player.equals(mario)) ServerPlayNetworking.send(player, packet);
		}
	}

	public static <T extends CustomPayload> CustomPayload.Id<T> makeID(String path) {
		return new CustomPayload.Id<>(MarioQuaMario.makeID(path));
	}

	protected record SyncUseCharacterStatsS2CPayload(boolean shouldUse) implements CustomPayload {
		public static final Id<SyncUseCharacterStatsS2CPayload> ID = MarioPackets.makeID("sync_use_character_stats_s2c");
		public static final PacketCodec<RegistryByteBuf, SyncUseCharacterStatsS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, SyncUseCharacterStatsS2CPayload::shouldUse,
				SyncUseCharacterStatsS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
