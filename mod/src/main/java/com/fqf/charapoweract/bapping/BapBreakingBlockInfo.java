package com.fqf.charapoweract.bapping;

import com.fqf.charapoweract_api.interfaces.BapResult;
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

		if(this.BAPPER instanceof PlayerEntity player) {
			// Code taken from ServerPlayerInteractionManager and ClientPlayerInteractionManager.

			BlockState blockState = this.WORLD.getBlockState(this.POS);
			Block block = blockState.getBlock();

			if(block instanceof OperatorBlock && !player.isCreativeLevelTwoOp()) {
				if(!this.WORLD.isClient)
					this.WORLD.updateListeners(this.POS, blockState, blockState, Block.NOTIFY_ALL);
				return null;
			}

			BlockState iDunnoWhatThisDoes = block.onBreak(this.WORLD, this.POS, blockState, player);

			boolean removedSuccessfully;

			if(this.WORLD.isClient) {
				FluidState fluidState = this.WORLD.getFluidState(this.POS);
				removedSuccessfully = this.WORLD.setBlockState(this.POS, fluidState.getBlockState(), Block.NOTIFY_ALL_AND_REDRAW);
			}
			else {
				removedSuccessfully = this.WORLD.removeBlock(this.POS, false);
			}

			if(removedSuccessfully) {
				block.onBroken(this.WORLD, this.POS, iDunnoWhatThisDoes);

				if(!this.WORLD.isClient && !player.isCreative())
					block.afterBreak(this.WORLD, player, this.POS, iDunnoWhatThisDoes, this.WORLD.getBlockEntity(this.POS), ItemStack.EMPTY);
			}
		}
		else this.WORLD.breakBlock(this.POS, true, this.BAPPER);



		return null;
	}
}
