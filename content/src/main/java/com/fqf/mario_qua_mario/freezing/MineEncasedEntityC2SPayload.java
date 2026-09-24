package com.fqf.mario_qua_mario.freezing;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record MineEncasedEntityC2SPayload(int entity, byte interaction) implements CustomPayload {
	private static final Id<MineEncasedEntityC2SPayload> ID = new Id<>(MarioQuaMario.makeID("mine_encased_entity_c2s"));
	private static final PacketCodec<RegistryByteBuf, MineEncasedEntityC2SPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, MineEncasedEntityC2SPayload::entity,
			PacketCodecs.BYTE, MineEncasedEntityC2SPayload::interaction,
			MineEncasedEntityC2SPayload::new
	);

	public static final byte START_MINING = 0;
	public static final byte STOP_MINING = 1;

	public static void receive(MineEncasedEntityC2SPayload payload, ServerPlayNetworking.Context context) {
		receive(
				(Entity & BreakableEntity) context.player().getWorld().getEntityById(payload.entity()),
				payload.interaction(),
				context
		);
	}

	private static <T extends Entity & BreakableEntity> void receive(T target, byte interaction, ServerPlayNetworking.Context context) {
		if(target == null || !target.mqm$canMine()) return;

		switch (interaction) {
			case START_MINING -> target.mqm$addMiner(context.player());
			case STOP_MINING -> target.mqm$stopMining(context.player());
			default -> throw new IllegalStateException(
					"Mario qua Mario: Unexpected value for MineEncasedEntityC2S Interaction byte: " + interaction
			);
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ID, MineEncasedEntityC2SPayload::receive);
	}
}
