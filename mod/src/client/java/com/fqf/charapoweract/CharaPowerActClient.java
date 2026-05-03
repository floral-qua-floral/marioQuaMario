package com.fqf.charapoweract;

import com.fqf.charapoweract.bapping.BumpedBlockParticle;
import com.fqf.charapoweract.packets.MarioClientPacketHelper;
import com.fqf.charapoweract.util.MarioClientEventListeners;
import com.fqf.charapoweract.util.MarioClientHelperManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class CharaPowerActClient implements ClientModInitializer {
	// This is in the client sources

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("CharaPowerAct initializing on the client...");

		MarioClientHelperManager.helper = new CPAClientHelper();
		MarioClientHelperManager.packetSender = new MarioClientPacketHelper();

		MarioClientPacketHelper.registerClientReceivers();

		MarioClientEventListeners.register();

		ParticleFactoryRegistry.getInstance().register(FabricParticleTypes.simple(), new BumpedBlockParticle.Factory());
	}
}