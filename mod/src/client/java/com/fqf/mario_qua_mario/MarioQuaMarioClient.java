package com.fqf.mario_qua_mario;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioQuaMarioClient implements ClientModInitializer {
	// This is in the client sources

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("Mario qua Mario Client initializing...");

		MarioAbstractClientHelper.instance = new MarioClientHelper();
	}
}