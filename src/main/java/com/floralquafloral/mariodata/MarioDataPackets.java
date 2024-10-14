package com.floralquafloral.mariodata;

import com.floralquafloral.MarioQuaMario;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.floralquafloral.mariodata.MarioDataManager.getMarioData;

public class MarioDataPackets {
	public void register() {

	}
	public void registerClient() {

	}

	public void setEnabled(PlayerEntity player, boolean enabled) {

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
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				getMarioData(context, payload.player).setEnabled(payload.isMario);
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
