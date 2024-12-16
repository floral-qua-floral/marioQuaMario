package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioDataPackets {
	public static void setActionS2C(
			ServerPlayerEntity mario, boolean networkToMario, AbstractParsedAction newAction,
			boolean doTransition, long seed)
	{
		ServerPlayNetworking.send(mario, new SetActionS2CPayload(
				mario.getId(), RegistryManager.ACTIONS.getRawIdOrThrow(newAction),
				doTransition, seed
		));
//		MarioPackets.sendToTrackers(
//				mario, new SetActionS2CPayload(
//						mario.getId(), RegistryManager.ACTIONS.getEntry(newAction),
//						doTransition, seed
//				), networkToMario
//		);
	}

	protected record SetActionC2SPayload(int newAction, long seed) implements CustomPayload {
		public static final Id<SetActionC2SPayload> ID = new Id<>(MarioQuaMario.makeID("set_action_c2s"));
		public static final PacketCodec<RegistryByteBuf, SetActionC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionC2SPayload::newAction,
				PacketCodecs.VAR_LONG, SetActionC2SPayload::seed,
				SetActionC2SPayload::new
		);

		public static void receive(SetActionC2SPayload payload, ServerPlayNetworking.Context context) {
			AbstractParsedAction action = RegistryManager.ACTIONS.get(payload.newAction());
			if(context.player().mqm$getMarioData().setActionInternal(action, payload.seed, false)) {
				MarioPackets.sendToTrackers(context.player(), new SetActionS2CPayload(
						context.player().getId(),
						payload.newAction,
						true,
						payload.seed
				), false);
			}
			else {
				ServerPlayNetworking.send(context.player(), new SetActionS2CPayload(
						context.player().getId(),
						RegistryManager.ACTIONS.getRawIdOrThrow(context.player().mqm$getMarioData().getAction()),
						false,
						payload.seed
				));
			}
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, SetActionC2SPayload::receive);
		}
	}

	protected record SetActionS2CPayload(int marioID, int newAction, boolean doTransition, long seed) implements CustomPayload {
		public static final Id<SetActionS2CPayload> ID = new Id<>(MarioQuaMario.makeID("set_action_s2c"));
		public static final PacketCodec<RegistryByteBuf, SetActionS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionS2CPayload::marioID,
				PacketCodecs.INTEGER, SetActionS2CPayload::newAction,
				PacketCodecs.BOOL, SetActionS2CPayload::doTransition,
				PacketCodecs.VAR_LONG, SetActionS2CPayload::seed,
				SetActionS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
