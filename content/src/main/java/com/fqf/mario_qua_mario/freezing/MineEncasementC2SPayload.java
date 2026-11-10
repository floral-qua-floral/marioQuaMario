package com.fqf.mario_qua_mario.freezing;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record MineEncasementC2SPayload(int encasement, byte interaction) implements CustomPayload {
	private static final Id<MineEncasementC2SPayload> ID = new Id<>(MarioQuaMario.makeID("mine_encasement_c2s"));
	private static final PacketCodec<RegistryByteBuf, MineEncasementC2SPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, MineEncasementC2SPayload::encasement,
			PacketCodecs.BYTE, MineEncasementC2SPayload::interaction,
			MineEncasementC2SPayload::new
	);

	public static final byte START_MINING = 0;
	public static final byte STOP_MINING = 1;

	public static void receive(MineEncasementC2SPayload payload, ServerPlayNetworking.Context context) {
		receive(
				(IceEncasementEntity) context.player().getWorld().getEntityById(payload.encasement()),
				payload.interaction(),
				context
		);
	}

	private static void receive(IceEncasementEntity target, byte interaction, ServerPlayNetworking.Context context) {
		if(target == null || !target.canHit()) return;

		switch (interaction) {
			case START_MINING -> target.addMiner(context.player());
			case STOP_MINING -> target.removeMiner(context.player());
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
		ServerPlayNetworking.registerGlobalReceiver(ID, MineEncasementC2SPayload::receive);
	}
}
