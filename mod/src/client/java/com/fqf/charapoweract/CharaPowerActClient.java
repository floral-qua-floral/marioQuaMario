package com.fqf.charapoweract;

import com.fqf.charapoweract.bapping.BumpedBlockParticle;
import com.fqf.charapoweract.packets.CPAClientPacketHelper;
import com.fqf.charapoweract.util.CharaPowerActClientEventListeners;
import com.fqf.charapoweract.util.CPAClientHelperManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class CharaPowerActClient implements ClientModInitializer {
	// This is in the client sources

	@Override
	public void onInitializeClient() {
		CharaPowerAct.LOGGER.info("CharaPowerAct initializing on the client...");

		CPAClientHelperManager.helper = new CPAClientHelper();
		CPAClientHelperManager.packetSender = new CPAClientPacketHelper();

		CPAClientPacketHelper.registerClientReceivers();

		CharaPowerActClientEventListeners.register();

		ParticleFactoryRegistry.getInstance().register(FabricParticleTypes.simple(), new BumpedBlockParticle.Factory());
	}
}