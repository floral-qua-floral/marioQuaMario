package com.fqf.charaformact.bapping;

import com.fqf.charaformact_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BumpingEmbrittlingBlockInfo extends BumpingBlockInfo {
	public BumpingEmbrittlingBlockInfo(World world, BlockPos pos, BapResult result, Direction direction, Entity bapper) {
		super(world, pos, result, direction, bapper);
	}

	@Override
	public AbstractBapInfo finishAndGetReplacement() {
		super.finishAndGetReplacement();
		return new EmbrittledBlockInfo(this.WORLD, this.POS, this.BAPPER);
	}
}
