package com.floralquafloral;

import com.floralquafloral.bumping.BumpManagerClient;
import com.floralquafloral.bumping.BumpedBlockParticle;
import com.floralquafloral.mariodata.MarioClientSideDataImplementation;
import com.floralquafloral.mariodata.MarioDataManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class MarioQuaMarioClient implements ClientModInitializer {

	public static final SimpleOption<Boolean> ALWAYS_FALSE = SimpleOption.ofBoolean("alwaysFalseOption", false);

	public static final List<Entity> SQUASHED_ENTITIES = Lists.newArrayList();

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("MarioQuaMarioClient.java loaded on {}", FabricLoader.getInstance().getEnvironmentType());
		MarioPackets.registerClient();

		MarioDataManager.registerClientEventListeners();
		BumpManagerClient.registerClientEventListeners();

		MarioClientSideDataImplementation.VoiceSoundEventInitializer.initialize();

		ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> SQUASHED_ENTITIES.remove(entity));
		ClientTickEvents.END_WORLD_TICK.register((world) -> SQUASHED_ENTITIES.removeIf(squashedEntity ->
				squashedEntity instanceof LivingEntity squashedLivingEntity && !squashedLivingEntity.isDead()));

		ParticleFactoryRegistry.getInstance().register(MarioQuaMario.GAMER, new BumpedBlockParticle.Factory());
	}
}
