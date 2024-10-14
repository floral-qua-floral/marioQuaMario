package com.floralquafloral.mariodata;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static com.floralquafloral.mariodata.MarioDataManager.getMarioData;

public class MarioDataPackets {
	public static void registerCommon() {
		SetEnabledS2CPayload.register();
	}
	public static void registerClient() {
		SetEnabledS2CPayload.registerReceiver();
	}

	public static void setMarioEnabled(ServerPlayerEntity player, boolean enabled) {
		getMarioData(player).setEnabled(enabled);
		MarioPackets.sendPacketToTrackers(player, true, new MarioDataPackets.SetEnabledS2CPayload(player, enabled));
	}

	public record SetEnabledS2CPayload(int player, boolean isMario) implements CustomPayload {
		public static final Id<SetEnabledS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_enabled"));
		public static final PacketCodec<RegistryByteBuf, SetEnabledS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetEnabledS2CPayload::player,
				PacketCodecs.BOOL, SetEnabledS2CPayload::isMario,
				SetEnabledS2CPayload::new
		);
		public SetEnabledS2CPayload(PlayerEntity player, boolean isMario) {
			this(player.getId(), isMario);
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					getMarioData(context, payload.player).setEnabled(payload.isMario));
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
