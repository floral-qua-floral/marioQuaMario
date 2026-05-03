package com.fqf.charapoweract_api.cpadata.util;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface CollisionMatcher {
	boolean test(RecordedCollision collision, BlockState block);
}
