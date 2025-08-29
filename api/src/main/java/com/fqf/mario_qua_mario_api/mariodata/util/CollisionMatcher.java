package com.fqf.mario_qua_mario_api.mariodata.util;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface CollisionMatcher {
	boolean test(RecordedCollision collision, BlockState block);
}
