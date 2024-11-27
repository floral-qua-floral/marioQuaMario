package com.floralquafloral.registries.states;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.MarioAttackInterceptingStateDefinition;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.RegistryManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AttackInterceptionHandler {
	public static BlockPos NONEXISTENT_BLOCKPOS = new BlockPos(0, -100, 0);

	public static void registerPackets() {
		InterceptedAttackC2SPayload.register();
		InterceptedAttackS2CPayload.register();

		InterceptedAttackC2SPayload.registerReceiver();
	}
	public static void registerPacketsClient() {
		InterceptedAttackS2CPayload.registerReceiver();
	}

	public static boolean attemptInterceptions(
			MarioMainClientData data, float attackCooldownProgress,
			List<MarioAttackInterceptingStateDefinition.AttackInterceptionDefinition> interceptions, boolean isFromAction,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
	) {
		for(MarioAttackInterceptingStateDefinition.AttackInterceptionDefinition interception : interceptions) {
			if(interception.shouldIntercept(data, attackCooldownProgress, targetEntity, targetBlock)) {
				long seed = Random.create().nextLong();
				interception.executeTravellers(data, targetEntity, targetBlock);
				data.applyModifiedVelocity();
				interception.executeClients(data, true, seed, targetEntity, targetBlock);

				Identifier actionTargetID = interception.getActionTarget();
				if(actionTargetID != null)
					data.setActionTransitionless(Objects.requireNonNull(RegistryManager.ACTIONS.get(actionTargetID)));

				ClientPlayNetworking.send(new InterceptedAttackC2SPayload(
						targetEntity == null ? -1 : targetEntity.getId(),
						targetBlock == null ? NONEXISTENT_BLOCKPOS : targetBlock,
						interceptions.indexOf(interception),
						isFromAction,
						seed
				));
				return true;
			}
		}
		return false;
	}

	public static void performInterceptionServer(
			MarioServerData data, int interceptionIndex, boolean isFromAction,
			int targetEntityID, @NotNull BlockPos targetBlock, long seed
	) {
		MarioQuaMario.LOGGER.info("Perform interception on server!");

		List<MarioAttackInterceptingStateDefinition.AttackInterceptionDefinition> interceptions =
				isFromAction ? data.getAction().INTERCEPTIONS : data.getPowerUp().INTERCEPTIONS;
		if(interceptionIndex > interceptions.size()) {
			MarioQuaMario.LOGGER.error(
					"{} tried to do an invalid Attack Interception?! Index {}, from {} ({}).",
					data.getMario().getName().getString(),
					interceptionIndex,
					isFromAction ? "action" : "power-up",
					isFromAction ? data.getAction().ID : data.getPowerUp().ID
			);
			return;
		}
		MarioAttackInterceptingStateDefinition.AttackInterceptionDefinition interception = interceptions.get(interceptionIndex);

		Entity targetEntity = targetEntityID == -1 ? null : data.getMario().getServerWorld().getEntityById(targetEntityID);

		interception.executeTravellers(data, targetEntity, targetBlock.equals(NONEXISTENT_BLOCKPOS) ? null : targetBlock);
		data.applyModifiedVelocity();
		if(targetEntity != null) interception.strikeEntity(
				data, data.getMario().getAttackCooldownProgress(0.5F),
				data.getMario().getServerWorld(), targetEntity
		);

		Identifier actionTargetID = interception.getActionTarget();
		if(actionTargetID != null)
			data.setActionTransitionless(Objects.requireNonNull(RegistryManager.ACTIONS.get(actionTargetID)));

		MarioPackets.sendPacketToTrackersExclusive(data.getMario(), new InterceptedAttackS2CPayload(
				data.getMario().getId(), targetEntityID, targetBlock, interceptionIndex, isFromAction, seed
		));
	}

	public static void performInterceptionClient(
			MarioPlayerData data, MarioClientSideData clientData, int interceptionIndex, boolean isFromAction,
			int targetEntityID, @NotNull BlockPos targetBlock, long seed
	) {
		List<MarioAttackInterceptingStateDefinition.AttackInterceptionDefinition> interceptions =
				isFromAction ? data.getAction().INTERCEPTIONS : data.getPowerUp().INTERCEPTIONS;
		MarioAttackInterceptingStateDefinition.AttackInterceptionDefinition interception = interceptions.get(interceptionIndex);

		Entity targetEntity = targetEntityID == -1 ? null : data.getMario().getWorld().getEntityById(targetEntityID);

		interception.executeClients(clientData, false, seed, targetEntity, targetBlock.equals(NONEXISTENT_BLOCKPOS) ? null : targetBlock);

		Identifier actionTargetID = interception.getActionTarget();
		if(actionTargetID != null)
			data.setActionTransitionless(Objects.requireNonNull(RegistryManager.ACTIONS.get(actionTargetID)));
	}

	private record InterceptedAttackC2SPayload(int targetEntity, BlockPos targetBlock, int interceptionIndex, boolean isFromAction, long seed) implements CustomPayload {
		public static final CustomPayload.Id<InterceptedAttackC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(MarioQuaMario.MOD_ID, "intercepted_attack_c2s"));
		public static final PacketCodec<RegistryByteBuf, InterceptedAttackC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, InterceptedAttackC2SPayload::targetEntity,
				BlockPos.PACKET_CODEC, InterceptedAttackC2SPayload::targetBlock,
				PacketCodecs.INTEGER, InterceptedAttackC2SPayload::interceptionIndex,
				PacketCodecs.BOOL, InterceptedAttackC2SPayload::isFromAction,
				PacketCodecs.VAR_LONG, InterceptedAttackC2SPayload::seed,
				InterceptedAttackC2SPayload::new
		);
		public static void registerReceiver() {
			ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(context.player());
				performInterceptionServer(data, payload.interceptionIndex, payload.isFromAction, payload.targetEntity, payload.targetBlock, payload.seed);
			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
		}
	}

	private record InterceptedAttackS2CPayload(int player, int targetEntity, BlockPos targetBlock, int interceptionIndex, boolean isFromAction, long seed) implements CustomPayload {
		public static final CustomPayload.Id<InterceptedAttackS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(MarioQuaMario.MOD_ID, "intercepted_attack_s2c"));
		public static final PacketCodec<RegistryByteBuf, InterceptedAttackS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, InterceptedAttackS2CPayload::player,
				PacketCodecs.INTEGER, InterceptedAttackS2CPayload::targetEntity,
				BlockPos.PACKET_CODEC, InterceptedAttackS2CPayload::targetBlock,
				PacketCodecs.INTEGER, InterceptedAttackS2CPayload::interceptionIndex,
				PacketCodecs.BOOL, InterceptedAttackS2CPayload::isFromAction,
				PacketCodecs.VAR_LONG, InterceptedAttackS2CPayload::seed,
				InterceptedAttackS2CPayload::new
		);
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioPlayerData data = MarioDataManager.getMarioData(context.player());
				MarioClientSideData clientData = (MarioClientSideData) data;
				performInterceptionClient(data, clientData, payload.interceptionIndex, payload.isFromAction, payload.targetEntity, payload.targetBlock, payload.seed);
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
