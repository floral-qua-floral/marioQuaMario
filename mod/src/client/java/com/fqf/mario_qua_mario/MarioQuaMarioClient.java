package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.bapping.BumpedBlockParticle;
import com.fqf.mario_qua_mario.packets.MarioClientPacketHelper;
import com.fqf.mario_qua_mario.util.MarioClientEventListeners;
import com.fqf.mario_qua_mario.util.MarioClientHelperManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class MarioQuaMarioClient implements ClientModInitializer {
	// This is in the client sources

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("Mario qua Mario Client initializing...");

		MarioClientHelperManager.helper = new MarioClientHelper();
		MarioClientHelperManager.packetSender = new MarioClientPacketHelper();

		MarioClientPacketHelper.registerClientReceivers();

		MarioClientEventListeners.register();

		ParticleFactoryRegistry.getInstance().register(FabricParticleTypes.simple(), new BumpedBlockParticle.Factory());
	}
}