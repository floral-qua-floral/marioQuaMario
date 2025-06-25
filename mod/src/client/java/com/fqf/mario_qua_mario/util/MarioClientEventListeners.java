package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.bapping.BlockBappingClientUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MarioClientEventListeners {
	public static void register() {
		ClientTickEvents.START_WORLD_TICK.register(BlockBappingClientUtil::clientWorldTick);
	}
}
