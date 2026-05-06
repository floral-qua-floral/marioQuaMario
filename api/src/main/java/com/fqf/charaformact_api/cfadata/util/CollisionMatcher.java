package com.fqf.charaformact_api.cfadata.util;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface CollisionMatcher {
	boolean test(RecordedCollision collision, BlockState block);
}
