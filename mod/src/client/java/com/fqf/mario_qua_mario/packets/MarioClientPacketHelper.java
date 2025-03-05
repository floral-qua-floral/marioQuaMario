package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioClientHelperManager;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.registries.ParsedAttackInterception;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MarioClientPacketHelper implements MarioClientHelperManager.ClientPacketSender {
	public static void registerClientReceivers() {
		// DisableMarioS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.DisableMarioS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().disableInternal()
		);

		// ActionTransitionS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.ActionTransitionS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().setAction(
					ParsedActionHelper.get(payload.fromAction()),
					ParsedActionHelper.get(payload.toAction()),
					payload.seed(), true, false
				)
		);

		// AssignActionS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.AssignActionS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().setActionTransitionless(
					ParsedActionHelper.get(payload.newAction())
				)
		);

		// EmpowerRevertS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.EmpowerRevertS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().setPowerUp(
						RegistryManager.POWER_UPS.get(payload.toPower()), payload.isReversion(), payload.seed()
				)
		);

		// AssignPowerUpS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.AssignPowerUpS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().setPowerUpTransitionless(
						RegistryManager.POWER_UPS.get(payload.newPower())
				)
		);

		// AssignCharacterS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.AssignCharacterS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().setCharacter(
						RegistryManager.CHARACTERS.get(payload.newCharacter())
				)
		);

		// SyncMarioDataS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.SyncMarioDataS2CPayload.ID, (payload, context) -> {
			MarioPlayerData data = getMarioFromID(context, payload.marioID()).mqm$getMarioData();
			data.setCharacter(RegistryManager.CHARACTERS.get(payload.character()));
			data.setPowerUpTransitionless(RegistryManager.POWER_UPS.get(payload.powerUp()));
			data.setActionTransitionless(RegistryManager.ACTIONS.get(payload.action()));
		});

		// MissedAttackInterceptedS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioAttackInterceptionPackets.MissedAttackInterceptedS2CPayload.ID, (payload, context) ->
				ParsedAttackInterception.getInterception(payload).execute(
						getMarioFromID(context, payload.marioID()).mqm$getMarioData(),
						null, null, payload.seed()
				)
		);
		// EntityAttackInterceptedS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioAttackInterceptionPackets.EntityAttackInterceptedS2CPayload.ID, (payload, context) ->
				ParsedAttackInterception.getInterception(payload).execute(
						getMarioFromID(context, payload.marioID()).mqm$getMarioData(),
						context.player().getWorld().getEntityById(payload.targetID()), null, payload.seed()
				)
		);
		// BlockAttackInterceptedS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioAttackInterceptionPackets.BlockAttackInterceptedS2CPayload.ID, (payload, context) ->
				ParsedAttackInterception.getInterception(payload).execute(
						getMarioFromID(context, payload.marioID()).mqm$getMarioData(),
						null, payload.targetBlock(), payload.seed()
				)
		);
	}

	public static PlayerEntity getMarioFromID(ClientPlayNetworking.Context context, int marioID) {
		return (PlayerEntity) Objects.requireNonNull(context.player().getWorld().getEntityById(marioID));
	}

	@Override
	public void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed) {
//		MarioQuaMario.LOGGER.info("Sending setActionC2S Packet for {}->{}", fromAction.ID, toAction.ID);
		ClientPlayNetworking.send(new MarioDataPackets.SetActionC2SPayload(fromAction.getIntID(), toAction.getIntID(), seed));
	}

	public static void attackInterceptionC2S(
			MarioMainClientData data, ParsedAttackInterception interception,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock, long seed
	) {
		CustomPayload packet;
		int interceptionSource;
		int interceptionIndex;

		if(interception.IS_FROM_ACTION) {
			interceptionSource = RegistryManager.ACTIONS.getRawIdOrThrow(data.getAction());
			interceptionIndex = data.getAction().INTERCEPTIONS.indexOf(interception);
		}
		else {
			interceptionSource = RegistryManager.POWER_UPS.getRawIdOrThrow(data.getPowerUp());
			interceptionIndex = data.getPowerUp().INTERCEPTIONS.indexOf(interception);
		}

		if(targetEntity != null)
			packet = new MarioAttackInterceptionPackets.EntityAttackInterceptedC2SPayload(
					interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, targetEntity.getId(), seed);
		else if(targetBlock != null)
			packet = new MarioAttackInterceptionPackets.BlockAttackInterceptedC2SPayload(
					interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, targetBlock, seed);
		else
			packet = new MarioAttackInterceptionPackets.MissedAttackInterceptedC2SPayload(
					interception.IS_FROM_ACTION, interceptionSource, interceptionIndex, seed);

		ClientPlayNetworking.send(packet);
	}
}
