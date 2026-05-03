package com.fqf.charapoweract_api.mariodata.util;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface CollisionMatcher {
	boolean test(RecordedCollision collision, BlockState block);
}
