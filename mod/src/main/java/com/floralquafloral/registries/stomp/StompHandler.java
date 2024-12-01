package com.floralquafloral.registries.stomp;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.StompableEntity;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.RegistryManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static com.floralquafloral.mariodata.MarioDataManager.getMarioData;

public class StompHandler {
	public static final TagKey<DamageType> USES_FEET_ITEM_TAG = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MarioQuaMario.MOD_ID, "uses_feet_item"));
	public static final TagKey<DamageType> USES_LEGS_ITEM_TAG = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MarioQuaMario.MOD_ID, "uses_legs_item"));
	public static final TagKey<DamageType> FLATTENS_ENTITIES_TAG = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MarioQuaMario.MOD_ID, "flattens_entities"));

	public static final TagKey<EntityType<?>> UNSTOMPABLE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MarioQuaMario.MOD_ID, "unstompable"));
	public static final TagKey<EntityType<?>> HURTS_TO_STOMP_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MarioQuaMario.MOD_ID, "hurts_to_stomp"));
	public static final TagKey<EntityType<?>> IMMUNE_TO_BASIC_STOMP_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MarioQuaMario.MOD_ID, "immune_to_basic_stomp"));

	public static void registerPackets() {
		ExecuteStompS2CPayload.register();
	}
	public static void registerPacketsClient() {
		ExecuteStompS2CPayload.registerReceiver();
	}

	public static void networkStomp(ServerPlayerEntity mario, Entity target, ParsedStomp stompType, StompableEntity.StompResult result, long seed) {
		MarioPackets.sendPacketToTrackers(mario, new ExecuteStompS2CPayload(mario, stompType, target, result, seed));
	}

	private record ExecuteStompS2CPayload(int player, int stompType, int target, int resultOrdinal, long seed) implements CustomPayload {
		public static final Id<ExecuteStompS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "execute_stomp"));
		public static final PacketCodec<RegistryByteBuf, ExecuteStompS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, ExecuteStompS2CPayload::player,
				PacketCodecs.INTEGER, ExecuteStompS2CPayload::stompType,
				PacketCodecs.INTEGER, ExecuteStompS2CPayload::target,
				PacketCodecs.INTEGER, ExecuteStompS2CPayload::resultOrdinal,
				PacketCodecs.VAR_LONG, ExecuteStompS2CPayload::seed,
				ExecuteStompS2CPayload::new
		);
		public ExecuteStompS2CPayload(PlayerEntity player, ParsedStomp stompType, Entity target, StompableEntity.StompResult result, long seed) {
			this(player.getId(), RegistryManager.STOMP_TYPES.getRawIdOrThrow(stompType), target.getId(), result.ordinal(), seed);
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				ParsedStomp stompType = RegistryManager.STOMP_TYPES.get(payload.stompType);
				PlayerEntity mario = (PlayerEntity) context.player().getWorld().getEntityById(payload.player);
				Entity target = context.player().getWorld().getEntityById(payload.target);

				if(mario == null || stompType == null || target == null) {
					MarioQuaMario.LOGGER.error("Execute Stomp S2C packet had invalid information!");
					MarioQuaMario.LOGGER.error("Mario: {}", mario);
					MarioQuaMario.LOGGER.error("Stomp Type: {}", stompType);
					MarioQuaMario.LOGGER.error("Target: {}", target);
					return;
				}
				MarioClientSideData data = (MarioClientSideData) getMarioData(mario);
				stompType.executeClient(data, mario.isMainPlayer(), target, StompableEntity.StompResult.values()[payload.resultOrdinal()], payload.seed);
//				data.setActionTransitionless(RegistryManager.ACTIONS.get(stompType.POST_STOMP_ACTION));
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
