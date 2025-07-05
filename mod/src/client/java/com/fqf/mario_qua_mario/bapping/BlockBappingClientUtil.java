package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class BlockBappingClientUtil {
	public static void clientWorldTick(ClientWorld world) {
		BlockBappingUtil.commonWorldTick(world);
	}

	public static final Map<ClientWorld, Map<BlockPos, BumpedBlockParticle>> PARTICLES = new HashMap<>();

	public static void clientBap(AbstractBapInfo info) {
		if(info instanceof BumpingBlockInfo bumpingInfo) {
			ClientWorld clientWorld = (ClientWorld) info.WORLD;

			BumpedBlockParticle newParticle = new BumpedBlockParticle(clientWorld,
					info.POS, bumpingInfo.DISPLACEMENT_DIRECTION, info instanceof BapBreakingBlockInfo);

			if(!PARTICLES.containsKey(clientWorld)) PARTICLES.put(clientWorld, new HashMap<>());
			BumpedBlockParticle oldParticle = PARTICLES.get(clientWorld).put(info.POS, newParticle);
			if(oldParticle != null) oldParticle.markDead();

			MinecraftClient.getInstance().particleManager.addParticle(newParticle);
		}
	}
}
