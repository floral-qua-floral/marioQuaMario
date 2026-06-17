package com.fqf.charaformact.packets;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.ParsedCollisionAttack;
import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact.registries.RegistryManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;

public class CfaPackets {
	public static void register() {
		CfaDataPackets.SetNoCharacterS2CPayload.register();

		CfaDataPackets.ActionTransitionS2CPayload.register();
		CfaDataPackets.AssignActionS2CPayload.register();

		CfaDataPackets.SetActionC2SPayload.register();

		CfaDataPackets.EmpowerRevertS2CPayload.register();
		CfaDataPackets.AssignFormS2CPayload.register();

		CfaDataPackets.AssignCharacterS2CPayload.register();

		CfaDataPackets.SyncCfaDataS2CPayload.register();

		CfaDataPackets.TransmitWallYawC2SPayload.register();
		CfaDataPackets.TransmitWallYawS2CPayload.register();

		SyncUseCharacterStatsS2CPayload.register();
		SyncCfaGamerulesS2C.register();

		CollisionAttackS2CPayload.register();
		CollisionAttackDragonPartAffectAttackerS2CPayload.register();
		CollisionAttackDragonPartNoAffectAttackerS2CPayload.register();

		AttackInterceptionPackets.MissedAttackInterceptedC2SPayload.register();
		AttackInterceptionPackets.EntityAttackInterceptedC2SPayload.register();
		AttackInterceptionPackets.BlockAttackInterceptedC2SPayload.register();

		AttackInterceptionPackets.MissedAttackInterceptedS2CPayload.register();
		AttackInterceptionPackets.EntityAttackInterceptedS2CPayload.register();
		AttackInterceptionPackets.BlockAttackInterceptedS2CPayload.register();

		BappingPackets.BapBlockC2SPayload.register();
		BappingPackets.BapBlockS2CPayload.register();
	}

	private static CustomPayload makeSyncGamerulesPacket() {
		return new SyncCfaGamerulesS2C(
				CfaGamerules.useCharacterStats,
				CfaGamerules.restrictAdventureBapping,
				CfaGamerules.adventurePlayersBreakBrittleBlocks
		);
	}

	public static void syncGamerulesS2C(MinecraftServer server) {
		CustomPayload packet = makeSyncGamerulesPacket();
		for(ServerPlayerEntity player : PlayerLookup.all(server)) {
			ServerPlayNetworking.send(player, packet);
		}
	}

	public static void syncGamerulesS2C(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, makeSyncGamerulesPacket());
	}

	public static void collisionAttackS2C(ServerPlayerEntity attacker, ParsedCollisionAttack collisionAttack, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker) {
//		CharaFormAct.LOGGER.info("Sending collision attack packet to clients.\nTarget: {}\nTarget ID: {}", target, target.getId());
		sendToTrackers(attacker, makeCollisionAttackS2CPayload(attacker, collisionAttack, target, result, affectAttacker), true);
	}
	private static CustomPayload makeCollisionAttackS2CPayload(ServerPlayerEntity player, ParsedCollisionAttack collisionAttack, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker) {
		if(target instanceof EnderDragonPart targetedDragonPart) {
			// oh my god ender dragon parts are HORRIFIC
			// this is a dumb way to handle it but i don't wanna have to figure out how to cram more variables into a payload
			// if you're reading this feel free to PR it or something i guess
			// >:(

			int partIndex = Arrays.asList(targetedDragonPart.owner.getBodyParts()).indexOf(targetedDragonPart);
			// ^ probably slow but only runs when attacking an ender dragon part so literally who cares

			if(affectAttacker) return new CollisionAttackDragonPartAffectAttackerS2CPayload(
					player.getId(),
					RegistryManager.COLLISION_ATTACKS.getRawIdOrThrow(collisionAttack),
					targetedDragonPart.owner.getId(),
					partIndex,
					result.ordinal(),
					player.getRandom().nextLong()
			);
			else return new CollisionAttackDragonPartNoAffectAttackerS2CPayload(
					player.getId(),
					RegistryManager.COLLISION_ATTACKS.getRawIdOrThrow(collisionAttack),
					targetedDragonPart.owner.getId(),
					partIndex,
					result.ordinal(),
					player.getRandom().nextLong()
			);
		}

		return new CollisionAttackS2CPayload(
				player.getId(),
				RegistryManager.COLLISION_ATTACKS.getRawIdOrThrow(collisionAttack),
				target.getId(),
				result.ordinal(),
				affectAttacker,
				player.getRandom().nextLong()
		);
	}

	public static void sendToTrackers(ServerPlayerEntity player, CustomPayload packet, boolean includingSelf) {
		if(includingSelf) ServerPlayNetworking.send(player, packet);
		for(ServerPlayerEntity tracker : PlayerLookup.tracking(player)) {
			if(!tracker.equals(player)) ServerPlayNetworking.send(tracker, packet);
		}
	}

	public static <T extends CustomPayload> CustomPayload.Id<T> makeID(String path) {
		return new CustomPayload.Id<>(CharaFormAct.makeID(path));
	}

	protected record SyncUseCharacterStatsS2CPayload(boolean shouldUse) implements CustomPayload {
		public static final Id<SyncUseCharacterStatsS2CPayload> ID = CfaPackets.makeID("sync_use_character_stats_s2c");
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

	protected record SyncCfaGamerulesS2C(boolean useCharacterStats, boolean isRestricted, boolean canBreakBrittle) implements CustomPayload {
		public static final Id<SyncCfaGamerulesS2C> ID = CfaPackets.makeID("sync_restrict_adventure_bapping_s2c");
		public static final PacketCodec<RegistryByteBuf, SyncCfaGamerulesS2C> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, SyncCfaGamerulesS2C::isRestricted,
				PacketCodecs.BOOL, SyncCfaGamerulesS2C::isRestricted,
				PacketCodecs.BOOL, SyncCfaGamerulesS2C::canBreakBrittle,
				SyncCfaGamerulesS2C::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record CollisionAttackS2CPayload(int playerID, int collisionAttackID, int targetID, int collisionAttackResultIndex, boolean affectAttacker, long seed) implements CustomPayload {
		public static final Id<CollisionAttackS2CPayload> ID = CfaPackets.makeID("collision_attack_s2c");
		public static final PacketCodec<RegistryByteBuf, CollisionAttackS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, CollisionAttackS2CPayload::playerID,
				PacketCodecs.INTEGER, CollisionAttackS2CPayload::collisionAttackID,
				PacketCodecs.INTEGER, CollisionAttackS2CPayload::targetID,
				PacketCodecs.INTEGER, CollisionAttackS2CPayload::collisionAttackResultIndex,
				PacketCodecs.BOOL, CollisionAttackS2CPayload::affectAttacker,
				PacketCodecs.VAR_LONG, CollisionAttackS2CPayload::seed,
				CollisionAttackS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record CollisionAttackDragonPartAffectAttackerS2CPayload(int playerID, int collisionAttackID, int dragonID, int partIndex, int collisionAttackResultIndex, long seed) implements CustomPayload {
		public static final Id<CollisionAttackDragonPartAffectAttackerS2CPayload> ID = CfaPackets.makeID("collision_attack_dragon_part_s2c");
		public static final PacketCodec<RegistryByteBuf, CollisionAttackDragonPartAffectAttackerS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, CollisionAttackDragonPartAffectAttackerS2CPayload::playerID,
				PacketCodecs.INTEGER, CollisionAttackDragonPartAffectAttackerS2CPayload::collisionAttackID,
				PacketCodecs.INTEGER, CollisionAttackDragonPartAffectAttackerS2CPayload::dragonID,
				PacketCodecs.INTEGER, CollisionAttackDragonPartAffectAttackerS2CPayload::partIndex,
				PacketCodecs.INTEGER, CollisionAttackDragonPartAffectAttackerS2CPayload::collisionAttackResultIndex,
				PacketCodecs.VAR_LONG, CollisionAttackDragonPartAffectAttackerS2CPayload::seed,
				CollisionAttackDragonPartAffectAttackerS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record CollisionAttackDragonPartNoAffectAttackerS2CPayload(int playerID, int collisionAttackID, int dragonID, int partIndex, int collisionAttackResultIndex, long seed) implements CustomPayload {
		public static final Id<CollisionAttackDragonPartNoAffectAttackerS2CPayload> ID = CfaPackets.makeID("collision_attack_dragon_part_no_affect_s2c");
		public static final PacketCodec<RegistryByteBuf, CollisionAttackDragonPartNoAffectAttackerS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, CollisionAttackDragonPartNoAffectAttackerS2CPayload::playerID,
				PacketCodecs.INTEGER, CollisionAttackDragonPartNoAffectAttackerS2CPayload::collisionAttackID,
				PacketCodecs.INTEGER, CollisionAttackDragonPartNoAffectAttackerS2CPayload::dragonID,
				PacketCodecs.INTEGER, CollisionAttackDragonPartNoAffectAttackerS2CPayload::partIndex,
				PacketCodecs.INTEGER, CollisionAttackDragonPartNoAffectAttackerS2CPayload::collisionAttackResultIndex,
				PacketCodecs.VAR_LONG, CollisionAttackDragonPartNoAffectAttackerS2CPayload::seed,
				CollisionAttackDragonPartNoAffectAttackerS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
