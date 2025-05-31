package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.registries.ParsedAttackInterception;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MarioAttackInterceptionPackets {
	public static void handleInterceptionCommandAction(
			ServerPlayerEntity mario, AbstractParsedAction fromAction, int index,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
	) {
		long seed = mario.getRandom().nextLong();
		fromAction.INTERCEPTIONS.get(index).execute(mario.mqm$getMarioData(), targetEntity, targetBlock, seed);
		AttackInterceptionPayload dummyPayload = new MissedAttackInterceptedC2SPayload(true, RegistryManager.ACTIONS.getRawIdOrThrow(fromAction), index, seed);
		MarioPackets.sendToTrackers(mario, convertPayloadToS2C(mario, dummyPayload, targetEntity, targetBlock), true);
	}
	public static void handleInterceptionCommandPowerUp(
			ServerPlayerEntity mario, ParsedPowerUp fromPowerUp, int index,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
	) {
		long seed = mario.getRandom().nextLong();
		fromPowerUp.INTERCEPTIONS.get(index).execute(mario.mqm$getMarioData(), targetEntity, targetBlock, seed);
		AttackInterceptionPayload dummyPayload = new MissedAttackInterceptedC2SPayload(false, RegistryManager.POWER_UPS.getRawIdOrThrow(fromPowerUp), index, seed);
		MarioPackets.sendToTrackers(mario, convertPayloadToS2C(mario, dummyPayload, targetEntity, targetBlock), true);
	}
	private static void handleAttackInterception(
			ServerPlayerEntity mario, AttackInterceptionPayload payload,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock, long seed
	) {
		MarioQuaMario.LOGGER.info("Received attack interception payload!\nTarget Entity: {}", targetEntity);

		ParsedAttackInterception.getInterception(payload)
				.execute(mario.mqm$getMarioData(), targetEntity, targetBlock, seed);

		MarioPackets.sendToTrackers(mario, convertPayloadToS2C(mario, payload, targetEntity, targetBlock), false);
	}
	private static CustomPayload convertPayloadToS2C(
			ServerPlayerEntity mario, AttackInterceptionPayload payload,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
	) {
		if(targetEntity != null)
			return new EntityAttackInterceptedS2CPayload(
					mario.getId(), payload.isFromAction(), payload.interceptionSource(), payload.interceptionIndex(), targetEntity.getId(), payload.seed());
		else if(targetBlock != null)
			return new BlockAttackInterceptedS2CPayload(
					mario.getId(), payload.isFromAction(), payload.interceptionSource(), payload.interceptionIndex(), targetBlock, payload.seed());
		else
			return new MissedAttackInterceptedS2CPayload(
					mario.getId(), payload.isFromAction(), payload.interceptionSource(), payload.interceptionIndex(), payload.seed());
	}

	public interface AttackInterceptionPayload {
		boolean isFromAction();
		int interceptionSource();
		int interceptionIndex();
		long seed();
	}

	protected record MissedAttackInterceptedC2SPayload(
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<MissedAttackInterceptedC2SPayload> ID = new Id<>(MarioQuaMario.makeID("missed_attack_intercepted_c2s"));
		public static final PacketCodec<RegistryByteBuf, MissedAttackInterceptedC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, MissedAttackInterceptedC2SPayload::isFromAction,
				PacketCodecs.INTEGER, MissedAttackInterceptedC2SPayload::interceptionSource,
				PacketCodecs.INTEGER, MissedAttackInterceptedC2SPayload::interceptionIndex,

				PacketCodecs.VAR_LONG, MissedAttackInterceptedC2SPayload::seed,
				MissedAttackInterceptedC2SPayload::new
		);

		public static void receive(MissedAttackInterceptedC2SPayload payload, ServerPlayNetworking.Context context) {
			handleAttackInterception(context.player(), payload, null, null, payload.seed);
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, MissedAttackInterceptedC2SPayload::receive);
		}
	}

	protected record EntityAttackInterceptedC2SPayload(
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			int targetID, long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<EntityAttackInterceptedC2SPayload> ID = new Id<>(MarioQuaMario.makeID("entity_attack_intercepted_c2s"));
		public static final PacketCodec<RegistryByteBuf, EntityAttackInterceptedC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, EntityAttackInterceptedC2SPayload::isFromAction,
				PacketCodecs.INTEGER, EntityAttackInterceptedC2SPayload::interceptionSource,
				PacketCodecs.INTEGER, EntityAttackInterceptedC2SPayload::interceptionIndex,

				PacketCodecs.INTEGER, EntityAttackInterceptedC2SPayload::targetID,
				PacketCodecs.VAR_LONG, EntityAttackInterceptedC2SPayload::seed,
				EntityAttackInterceptedC2SPayload::new
		);

		public static void receive(EntityAttackInterceptedC2SPayload payload, ServerPlayNetworking.Context context) {
			handleAttackInterception(context.player(), payload,
					context.player().getServerWorld().getDragonPart(payload.targetID), null, payload.seed);
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, EntityAttackInterceptedC2SPayload::receive);
		}
	}

	protected record BlockAttackInterceptedC2SPayload(
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			BlockPos targetBlock, long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<BlockAttackInterceptedC2SPayload> ID = new Id<>(MarioQuaMario.makeID("block_attack_intercepted_c2s"));
		public static final PacketCodec<RegistryByteBuf, BlockAttackInterceptedC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, BlockAttackInterceptedC2SPayload::isFromAction,
				PacketCodecs.INTEGER, BlockAttackInterceptedC2SPayload::interceptionSource,
				PacketCodecs.INTEGER, BlockAttackInterceptedC2SPayload::interceptionIndex,

				BlockPos.PACKET_CODEC, BlockAttackInterceptedC2SPayload::targetBlock,
				PacketCodecs.VAR_LONG, BlockAttackInterceptedC2SPayload::seed,
				BlockAttackInterceptedC2SPayload::new
		);

		public static void receive(BlockAttackInterceptedC2SPayload payload, ServerPlayNetworking.Context context) {
			handleAttackInterception(context.player(), payload, null, payload.targetBlock, payload.seed);
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, BlockAttackInterceptedC2SPayload::receive);
		}
	}

	protected record MissedAttackInterceptedS2CPayload(
			int marioID,
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<MissedAttackInterceptedS2CPayload> ID = MarioPackets.makeID("missed_attack_intercepted_s2c");
		public static final PacketCodec<RegistryByteBuf, MissedAttackInterceptedS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, MissedAttackInterceptedS2CPayload::marioID,

				PacketCodecs.BOOL, MissedAttackInterceptedS2CPayload::isFromAction,
				PacketCodecs.INTEGER, MissedAttackInterceptedS2CPayload::interceptionSource,
				PacketCodecs.INTEGER, MissedAttackInterceptedS2CPayload::interceptionIndex,

				PacketCodecs.VAR_LONG, MissedAttackInterceptedS2CPayload::seed,
				MissedAttackInterceptedS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record EntityAttackInterceptedS2CPayload(
			int marioID,
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			int targetID, long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<EntityAttackInterceptedS2CPayload> ID = MarioPackets.makeID("entity_attack_intercepted_s2c");
		public static final PacketCodec<RegistryByteBuf, EntityAttackInterceptedS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, EntityAttackInterceptedS2CPayload::marioID,

				PacketCodecs.BOOL, EntityAttackInterceptedS2CPayload::isFromAction,
				PacketCodecs.INTEGER, EntityAttackInterceptedS2CPayload::interceptionSource,
				PacketCodecs.INTEGER, EntityAttackInterceptedS2CPayload::interceptionIndex,

				PacketCodecs.INTEGER, EntityAttackInterceptedS2CPayload::targetID,
				PacketCodecs.VAR_LONG, EntityAttackInterceptedS2CPayload::seed,
				EntityAttackInterceptedS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record BlockAttackInterceptedS2CPayload(
			int marioID,
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			BlockPos targetBlock, long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<BlockAttackInterceptedS2CPayload> ID = MarioPackets.makeID("block_attack_intercepted_s2c");
		public static final PacketCodec<RegistryByteBuf, BlockAttackInterceptedS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, BlockAttackInterceptedS2CPayload::marioID,

				PacketCodecs.BOOL, BlockAttackInterceptedS2CPayload::isFromAction,
				PacketCodecs.INTEGER, BlockAttackInterceptedS2CPayload::interceptionSource,
				PacketCodecs.INTEGER, BlockAttackInterceptedS2CPayload::interceptionIndex,

				BlockPos.PACKET_CODEC, BlockAttackInterceptedS2CPayload::targetBlock,
				PacketCodecs.VAR_LONG, BlockAttackInterceptedS2CPayload::seed,
				BlockAttackInterceptedS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
