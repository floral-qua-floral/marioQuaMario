package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioClientHelperManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class MarioClientPacketHelper implements MarioClientHelperManager.ClientPacketSender {
	public static void registerClientReceivers() {
		// ActionTransitionS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.ActionTransitionS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().setAction(
					ParsedActionHelper.get(payload.fromAction()),
					ParsedActionHelper.get(payload.toAction()),
					payload.seed(), true
				)
		);

		// AssignActionS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.AssignActionS2CPayload.ID, (payload, context) ->
				getMarioFromID(context, payload.marioID()).mqm$getMarioData().setActionTransitionless(
					ParsedActionHelper.get(payload.newAction())
				)
		);
	}

	public static PlayerEntity getMarioFromID(ClientPlayNetworking.Context context, int marioID) {
		return (PlayerEntity) Objects.requireNonNull(context.player().getWorld().getEntityById(marioID));
	}

	@Override
	public void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed) {
		ClientPlayNetworking.send(new MarioDataPackets.SetActionC2SPayload(fromAction.getIntID(), toAction.getIntID(), seed));
	}
}
