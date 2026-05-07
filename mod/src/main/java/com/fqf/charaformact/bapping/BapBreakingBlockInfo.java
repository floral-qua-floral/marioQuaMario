package com.fqf.charaformact.bapping;

import com.fqf.charaformact_api.interfaces.BapResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OperatorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BapBreakingBlockInfo extends BumpingBlockInfo {
	public BapBreakingBlockInfo(World world, BlockPos pos, BapResult result, Direction direction, Entity bapper) {
		super(world, pos, result, direction, bapper);
	}

//	@Override
//	protected long getFinishTime(World world) {
//		return world.getTime() + (BumpingBlockInfo.BUMP_DURATION / 2) + (world.isClient() ? 0 : -1);
//	}


	@Override
	public boolean isDone() {
		return this.WORLD.getTime() > this.FINISH_TIME - BumpingBlockInfo.BUMP_DURATION + 1;
	}

	@Override
	public AbstractBapInfo finishAndGetReplacement() {
		super.finishAndGetReplacement();

		if(this.BAPPER instanceof PlayerEntity player) BlockBappingUtil.mineBlockWithBap(this.WORLD, this.POS, player);
		else this.WORLD.breakBlock(this.POS, true, this.BAPPER);



		return null;
	}
}
