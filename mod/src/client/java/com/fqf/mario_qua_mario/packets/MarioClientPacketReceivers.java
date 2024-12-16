package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class MarioClientPacketReceivers {
	public static void registerClientReceivers() {
		// SetActionS2CPayload Receiver
		ClientPlayNetworking.registerGlobalReceiver(MarioDataPackets.SetActionS2CPayload.ID, (payload, context) -> {
			MarioPlayerData data = getMarioFromID(context, payload.marioID()).mqm$getMarioData();
			AbstractParsedAction action = RegistryManager.ACTIONS.get(payload.newAction());
			if(payload.doTransition()) data.setActionInternal(action, payload.seed(), true);
			else data.setActionTransitionlessInternal(action);
		});
	}

	public static PlayerEntity getMarioFromID(ClientPlayNetworking.Context context, int marioID) {
		return (PlayerEntity) Objects.requireNonNull(context.player().getWorld().getEntityById(marioID));
	}
}
