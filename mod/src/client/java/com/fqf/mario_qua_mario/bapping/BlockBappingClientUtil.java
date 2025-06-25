package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;

public class BlockBappingClientUtil {
	public static void clientWorldTick(ClientWorld world) {
		BlockBappingUtil.commonWorldTick(world);
	}

	public static void clientBap(AbstractBapInfo info) {
		if(info instanceof BumpingBlockInfo bumpingInfo)
			MinecraftClient.getInstance().particleManager.addParticle(new BumpedBlockParticle((ClientWorld) info.WORLD,
					info.POS, bumpingInfo.DISPLACEMENT_DIRECTION, info instanceof BapBreakingBlockInfo));
	}
}
