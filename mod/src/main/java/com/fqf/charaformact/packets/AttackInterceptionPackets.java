package com.fqf.charaformact.packets;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.ParsedAttackInterception;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.ParsedActionHelper;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
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

public class AttackInterceptionPackets {
	public static void handleInterceptionCommandAction(
			ServerPlayerEntity player, AbstractParsedAction fromAction, int index,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
	) {
		long seed = player.getRandom().nextLong();
		fromAction.INTERCEPTIONS.get(index).execute(player.cfa$getCfaData(), targetEntity, targetBlock, seed);
		AttackInterceptionPayload dummyPayload = new MissedAttackInterceptedC2SPayload(true, RegistryManager.ACTIONS.getRawIdOrThrow(fromAction), index, seed);
		CfaPackets.sendToTrackers(player, convertPayloadToS2C(player, dummyPayload, targetEntity, targetBlock), true);
	}
	public static void handleInterceptionCommandForm(
			ServerPlayerEntity player, ParsedForm fromForm, int index,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
	) {
		long seed = player.getRandom().nextLong();
		fromForm.INTERCEPTIONS.get(index).execute(player.cfa$getCfaData(), targetEntity, targetBlock, seed);
		AttackInterceptionPayload dummyPayload = new MissedAttackInterceptedC2SPayload(false, RegistryManager.FORMS.getRawIdOrThrow(fromForm), index, seed);
		CfaPackets.sendToTrackers(player, convertPayloadToS2C(player, dummyPayload, targetEntity, targetBlock), true);
	}
	private static void handleAttackInterception(
			ServerPlayerEntity player, AttackInterceptionPayload payload,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock, long seed
	) {
		// Validate interception source (form or action)!
		if(payload.isFromAction()) {
			if(!player.cfa$getCfaData().recentlyInAction(ParsedActionHelper.get(payload.interceptionSource()))) {
				logIllegalAttackInterception(player, payload, "Not recently in action!! Current action: "
						+ player.cfa$getCfaData().getAction().ID);
				return;
			}
		}
		else {
			if(player.cfa$getCfaData().getForm() != RegistryManager.FORMS.getOrThrow(payload.interceptionSource())) {
				logIllegalAttackInterception(player, payload, "Not in specified form! Current form: "
						+ player.cfa$getCfaData().getForm().ID);
				return;
			}
		}

		// Validate targets!
		if(targetBlock != null && !player.canInteractWithBlockAt(targetBlock, 1.0)) {
			targetBlock = null;
			logIllegalAttackInterception(player, payload, "Block is too far away!");
		}
		if(targetEntity != null && !player.canInteractWithEntity(targetEntity, 1.0)) {
			targetEntity = null;
			logIllegalAttackInterception(player, payload, "Entity is too far away!");
		}

		ParsedAttackInterception.getInterception(payload)
				.execute(player.cfa$getCfaData(), targetEntity, targetBlock, seed);

		CfaPackets.sendToTrackers(player, convertPayloadToS2C(player, payload, targetEntity, targetBlock), false);
	}
	private static void logIllegalAttackInterception(ServerPlayerEntity player, AttackInterceptionPayload payload, String extraDetails) {
		CharaFormAct.LOGGER.warn("{} attempted an illegal action transition: {} from {} {}. Details: {}",
				player.getName().getString(),
				payload.interceptionSource(),
				payload.isFromAction() ? "ACTION" : "FORM",
				ParsedAttackInterception.getStateOrigin(payload),
				extraDetails
		);
	}
	private static CustomPayload convertPayloadToS2C(
			ServerPlayerEntity player, AttackInterceptionPayload payload,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
	) {
		if(targetEntity != null)
			return new EntityAttackInterceptedS2CPayload(
					player.getId(), payload.isFromAction(), payload.interceptionSource(), payload.interceptionIndex(), targetEntity.getId(), payload.seed());
		else if(targetBlock != null)
			return new BlockAttackInterceptedS2CPayload(
					player.getId(), payload.isFromAction(), payload.interceptionSource(), payload.interceptionIndex(), targetBlock, payload.seed());
		else
			return new MissedAttackInterceptedS2CPayload(
					player.getId(), payload.isFromAction(), payload.interceptionSource(), payload.interceptionIndex(), payload.seed());
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
		public static final Id<MissedAttackInterceptedC2SPayload> ID = new Id<>(CharaFormAct.makeID("missed_attack_intercepted_c2s"));
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
		public static final Id<EntityAttackInterceptedC2SPayload> ID = new Id<>(CharaFormAct.makeID("entity_attack_intercepted_c2s"));
		public static final PacketCodec<RegistryByteBuf, EntityAttackInterceptedC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, EntityAttackInterceptedC2SPayload::isFromAction,
				PacketCodecs.INTEGER, EntityAttackInterceptedC2SPayload::interceptionSource,
				PacketCodecs.INTEGER, EntityAttackInterceptedC2SPayload::interceptionIndex,

				PacketCodecs.INTEGER, EntityAttackInterceptedC2SPayload::targetID,
				PacketCodecs.VAR_LONG, EntityAttackInterceptedC2SPayload::seed,
				EntityAttackInterceptedC2SPayload::new
		);

		public static void receive(EntityAttackInterceptedC2SPayload payload, ServerPlayNetworking.Context context) {
			//noinspection deprecation
			handleAttackInterception(context.player(), payload,
					context.player().getServerWorld().getDragonPart(payload.targetID), null, payload.seed);
			// vanilla code uses getDragonPart, why is it deprecated?????????
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
		public static final Id<BlockAttackInterceptedC2SPayload> ID = new Id<>(CharaFormAct.makeID("block_attack_intercepted_c2s"));
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
			int playerID,
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<MissedAttackInterceptedS2CPayload> ID = CfaPackets.makeID("missed_attack_intercepted_s2c");
		public static final PacketCodec<RegistryByteBuf, MissedAttackInterceptedS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, MissedAttackInterceptedS2CPayload::playerID,

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
			int playerID,
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			int targetID, long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<EntityAttackInterceptedS2CPayload> ID = CfaPackets.makeID("entity_attack_intercepted_s2c");
		public static final PacketCodec<RegistryByteBuf, EntityAttackInterceptedS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, EntityAttackInterceptedS2CPayload::playerID,

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
			int playerID,
			boolean isFromAction, int interceptionSource, int interceptionIndex,
			BlockPos targetBlock, long seed
	) implements CustomPayload, AttackInterceptionPayload {
		public static final Id<BlockAttackInterceptedS2CPayload> ID = CfaPackets.makeID("block_attack_intercepted_s2c");
		public static final PacketCodec<RegistryByteBuf, BlockAttackInterceptedS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, BlockAttackInterceptedS2CPayload::playerID,

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
