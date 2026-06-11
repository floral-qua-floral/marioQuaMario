package com.fqf.charaformact.packets;

import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.compat.RecordingModsCompatSafe;
import com.fqf.charaformact.registries.ParsedCollisionAttack;
import com.fqf.charaformact.registries.actions.TransitionPhase;
import com.fqf.charaformact.util.CfaClientHelperManager;
import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact_api.interfaces.BapResult;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact.cfadata.*;
import com.fqf.charaformact.registries.ParsedAttackInterception;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.ParsedActionHelper;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CfaClientPacketHelper implements CfaClientHelperManager.ClientPacketSender {
	public static void registerClientReceivers() {
		// SyncUseCharacterStatsS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaPackets.SyncUseCharacterStatsS2CPayload.ID, (payload, context) ->
				CfaGamerules.useCharacterStats = payload.shouldUse()
		);

		// SyncAdventureGamerulesS2C Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaPackets.SyncAdventureGamerulesS2C.ID, (payload, context) -> {
			CfaGamerules.restrictAdventureBapping = payload.isRestricted();
			CfaGamerules.adventurePlayersBreakBrittleBlocks = payload.canBreakBrittle();
		});

		// CollisionAttackS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaPackets.CollisionAttackS2CPayload.ID, (payload, context) -> {
			Entity target = context.player().getWorld().getEntityById(payload.targetID());
			packetAgnosticCollisionAttackHandling(context, payload.playerID(), target, payload.collisionAttackID(), payload.collisionAttackResultIndex(), payload.affectAttacker(), payload.seed());
		});

		// CollisionAttackDragonPartAffectAttackerS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaPackets.CollisionAttackDragonPartAffectAttackerS2CPayload.ID, (payload, context) -> {
			EnderDragonEntity dragon = (EnderDragonEntity) context.player().getWorld().getEntityById(payload.dragonID());
			assert dragon != null;
			Entity target = dragon.getBodyParts()[payload.partIndex()];
			packetAgnosticCollisionAttackHandling(context, payload.playerID(), target, payload.collisionAttackID(), payload.collisionAttackResultIndex(), true, payload.seed());
		});

		// CollisionAttackDragonPartNoAffectAttackerS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaPackets.CollisionAttackDragonPartNoAffectAttackerS2CPayload.ID, (payload, context) -> {
			EnderDragonEntity dragon = (EnderDragonEntity) context.player().getWorld().getEntityById(payload.dragonID());
			assert dragon != null;
			Entity target = dragon.getBodyParts()[payload.partIndex()];
			packetAgnosticCollisionAttackHandling(context, payload.playerID(), target, payload.collisionAttackID(), payload.collisionAttackResultIndex(), false, payload.seed());
		});

		// SetNoCharacterS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.SetNoCharacterS2CPayload.ID, (payload, context) ->
				getPlayerFromID(context, payload.playerID()).cfa$getCfaData().disableInternal()
		);

		// ActionTransitionS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.ActionTransitionS2CPayload.ID, (payload, context) ->
				getPlayerFromID(context, payload.playerID()).cfa$getCfaData().setAction(
					ParsedActionHelper.get(payload.fromAction()),
					ParsedActionHelper.get(payload.toAction()),
					payload.seed(), true, false
				)
		);

		// AssignActionS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.AssignActionS2CPayload.ID, (payload, context) ->
				getPlayerFromID(context, payload.playerID()).cfa$getCfaData().setActionTransitionless(
					ParsedActionHelper.get(payload.newAction())
				)
		);

		// EmpowerRevertS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.EmpowerRevertS2CPayload.ID, (payload, context) ->
				getPlayerFromID(context, payload.playerID()).cfa$getCfaData().setForm(
						RegistryManager.FORMS.get(payload.toForm()), payload.isReversion(), payload.seed()
				)
		);

		// AssignFormS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.AssignFormS2CPayload.ID, (payload, context) ->
				getPlayerFromID(context, payload.playerID()).cfa$getCfaData().setFormTransitionless(
						RegistryManager.FORMS.get(payload.newForm())
				)
		);

		// AssignCharacterS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.AssignCharacterS2CPayload.ID, (payload, context) ->
				getPlayerFromID(context, payload.playerID()).cfa$getCfaData().setCharacter(
						RegistryManager.CHARACTERS.get(payload.newCharacter())
				)
		);

		// SyncCfaDataS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.SyncCfaDataS2CPayload.ID, (payload, context) -> {
			CfaPlayerData data = getPlayerFromID(context, payload.playerID()).cfa$getCfaData();
			data.setCharacter(RegistryManager.CHARACTERS.get(payload.character()));
			data.setFormTransitionless(RegistryManager.FORMS.get(payload.form()));
			data.setActionTransitionless(RegistryManager.ACTIONS.get(payload.action()));
		});

		// TransmitWallYawS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(CfaDataPackets.TransmitWallYawS2CPayload.ID, (payload, context) -> {
			CfaPlayerData data = getPlayerFromID(context, payload.playerID()).cfa$getCfaData();
			data.getWallInfo().setYaw(payload.yaw());
		});

		// MissedAttackInterceptedS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(AttackInterceptionPackets.MissedAttackInterceptedS2CPayload.ID, (payload, context) ->
				ParsedAttackInterception.getInterception(payload).execute(
						getPlayerFromID(context, payload.playerID()).cfa$getCfaData(),
						null, null, payload.seed()
				)
		);
		// EntityAttackInterceptedS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(AttackInterceptionPackets.EntityAttackInterceptedS2CPayload.ID, (payload, context) ->
				ParsedAttackInterception.getInterception(payload).execute(
						getPlayerFromID(context, payload.playerID()).cfa$getCfaData(),
						context.player().getWorld().getEntityById(payload.targetID()), null, payload.seed()
				)
		);
		// BlockAttackInterceptedS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(AttackInterceptionPackets.BlockAttackInterceptedS2CPayload.ID, (payload, context) ->
				ParsedAttackInterception.getInterception(payload).execute(
						getPlayerFromID(context, payload.playerID()).cfa$getCfaData(),
						null, payload.targetBlock(), payload.seed()
				)
		);

		// BapBlockS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(BappingPackets.BapBlockS2CPayload.ID, (payload, context) -> {
			BlockBappingUtil.storeBapInfo(Objects.requireNonNull(BlockBappingUtil.makeBapInfo(
					context.player().clientWorld,
					payload.pos(),
					Direction.values()[payload.direction()],
					payload.strength(),
					context.player().clientWorld.getEntityById(payload.bapperID()),
					BapResult.values()[payload.result()]
			), "Received payload for a bap that shouldn't trigger bap packet sending??"), true);
		});
	}

	public static PlayerEntity getPlayerFromID(ClientPlayNetworking.Context context, int playerID) {
		return (PlayerEntity) Objects.requireNonNull(context.player().getWorld().getEntityById(playerID));
	}

	@Override
	public void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, TransitionPhase phase) {
		CustomPayload packet = new CfaDataPackets.SetActionC2SPayload(fromAction.getIntID(), toAction.getIntID(), seed);
		if(phase == TransitionPhase.WORLD_COLLISION) {
			// If this transition occurred after moving, hold onto it and only send it after we've sent the movement packet too.
			// This means that the player's position on the server when the transition is checked should match her position
			// when it was checked on the client (unless the movement itself was rejected by the server, i.e. moved wrongly).
			ClientPlayerEntity mainPlayer = MinecraftClient.getInstance().player;
			assert mainPlayer != null;
			mainPlayer.cfa$getCfaData().HELD_TRANSITION_PACKETS.add(packet);
			return;
		}
		ClientPlayNetworking.send(packet);
	}

	@Override
	public void conditionallySaveTransitionToReplayMod(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed) {
		assert MinecraftClient.getInstance().player != null;
		RecordingModsCompatSafe.conditionallySaveReplayPacket(new CfaDataPackets.ActionTransitionS2CPayload(
				MinecraftClient.getInstance().player.getId(),
				fromAction.getIntID(), toAction.getIntID(), seed
		));
	}

	public static void attackInterceptionC2S(
			CfaMainClientData data, ParsedAttackInterception interception,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock, long seed
	) {
		CustomPayload packet;
		CustomPayload replayPacket;
		int interceptionSource;
		int interceptionIndex;

		if(interception.IS_FROM_ACTION) {
			interceptionSource = RegistryManager.ACTIONS.getRawIdOrThrow(data.getAction());
			interceptionIndex = data.getAction().INTERCEPTIONS.indexOf(interception);
		}
		else {
			interceptionSource = RegistryManager.FORMS.getRawIdOrThrow(data.getForm());
			interceptionIndex = data.getForm().INTERCEPTIONS.indexOf(interception);
		}

		int playerID = data.getPlayer().getId();
		if(targetEntity != null) {
			packet = new AttackInterceptionPackets.EntityAttackInterceptedC2SPayload(
					interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, targetEntity.getId(), seed);
			replayPacket = new AttackInterceptionPackets.EntityAttackInterceptedS2CPayload(
					playerID, interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, targetEntity.getId(), seed);
		}
		else if(targetBlock != null) {
			packet = new AttackInterceptionPackets.BlockAttackInterceptedC2SPayload(
					interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, targetBlock, seed);
			replayPacket = new AttackInterceptionPackets.BlockAttackInterceptedS2CPayload(
					playerID, interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, targetBlock, seed);
		}
		else {
			packet = new AttackInterceptionPackets.MissedAttackInterceptedC2SPayload(
					interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, seed);
			replayPacket = new AttackInterceptionPackets.MissedAttackInterceptedS2CPayload(
					playerID, interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, seed);
		}

		ClientPlayNetworking.send(packet);
		RecordingModsCompatSafe.conditionallySaveReplayPacket(replayPacket);
	}

	public void transmitWallYawC2S(CfaMoveableData data, float wallYaw) {
		ClientPlayNetworking.send(new CfaDataPackets.TransmitWallYawC2SPayload(wallYaw));

		RecordingModsCompatSafe.conditionallySaveReplayPacket(
				new CfaDataPackets.TransmitWallYawS2CPayload(data.getPlayer().getId(), wallYaw));
	}

	private static void packetAgnosticCollisionAttackHandling(
			ClientPlayNetworking.Context context,
			int attackerID, Entity target,
			int collisionAttackID, int collisionAttackResultIndex, boolean affectAttacker, long seed
	) {
		PlayerEntity attacker = getPlayerFromID(context, attackerID);
		CfaPlayerData data = attacker.cfa$getCfaData();
		CollisionAttackResult.ExecutableResult result = CollisionAttackResult.ExecutableResult.values()[collisionAttackResultIndex];
		ParsedCollisionAttack collisionAttack = Objects.requireNonNull(RegistryManager.COLLISION_ATTACKS.get(collisionAttackID));

		if(affectAttacker) collisionAttack.transitionAction(data, result);

		collisionAttack.executeClients((CfaClientData) data, target, result, affectAttacker, seed);

		if(data instanceof CfaMoveableData moveableData) {
			Vec3d targetPos = collisionAttack.executeTravellersAndGetTargetPos(moveableData, target, result, attacker.getPos(), affectAttacker);
			if(affectAttacker && targetPos != null) {
				data.getPlayer().move(MovementType.SELF, targetPos.subtract(attacker.getPos()));
			}
		}
	}

	@Override
	public void bapBlockC2S(BlockPos pos, Direction direction, AbstractParsedAction action) {
		ClientPlayNetworking.send(new BappingPackets.BapBlockC2SPayload(pos, direction.ordinal(), action.getIntID()));
	}

	@Override
	public void conditionallySaveBapToReplayMod(BlockPos pos, Direction direction, int strength, BapResult result, Entity bapper) {
		RecordingModsCompatSafe.conditionallySaveReplayPacket(new BappingPackets.BapBlockS2CPayload(
				pos, direction.ordinal(), strength, result.ordinal(), bapper.getId()
		));
	}

	public static void syncCfaDatasToReplay() {
		// Known issue - might not include players in other dimensions being tracked through Immersive Portals?
		// WHO CARES!!!!!!!

		// Known issue that actually matters - doesn't save playermodel? :(
		for(PlayerEntity player : Objects.requireNonNull(MinecraftClient.getInstance().player).getWorld().getPlayers()) {
			CfaPlayerData data = player.cfa$getCfaData();
			if(data.isEnabled()) {
				RecordingModsCompatSafe.conditionallySaveReplayPacket(new CfaDataPackets.SyncCfaDataS2CPayload(
						player.getId(),
						RegistryManager.CHARACTERS.getRawIdOrThrow(data.getCharacter()),
						RegistryManager.FORMS.getRawIdOrThrow(data.getForm()),
						data.getAction().getIntID()
				));
			}
		}
	}
}
