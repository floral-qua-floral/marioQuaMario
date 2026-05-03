package com.fqf.charapoweract_api.mixin.bappables;

import com.fqf.charapoweract_api.interfaces.BapResult;
import com.fqf.charapoweract_api.interfaces.Bappable;
import com.fqf.charapoweract_api.cpadata.ICPAData;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TrapdoorBlock.class)
public abstract class TrapdoorBlockMixin extends HorizontalFacingBlock implements Bappable {
	protected TrapdoorBlockMixin(Settings settings) {
		super(settings);
		throw new IllegalStateException("Trying to use mixin constructor???");
	}

	@Shadow @Final public static BooleanProperty OPEN;

	@Shadow @Final public static EnumProperty<BlockHalf> HALF;

	@Shadow protected abstract void flip(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player);

	@Override
	public void cpa$onBapped(ICPAData data, World world, BlockPos pos, BlockState blockState, Direction direction, int strength, BapResult result) {
		Bappable.super.cpa$onBapped(data, world, pos, blockState, direction, strength, result);

		switch(result) {
			case BUMP, BUMP_WITHOUT_POWERING, BUMP_EMBRITTLE, BUMP_EMBRITTLE_WITHOUT_POWERING, BREAK,
				 BREAK_WITHOUT_POWERING -> {
				if(canBeFlippedTowards(blockState, direction))
					this.flip(blockState, world, pos, data == null ? null : data.getPlayer());
			}
		}
	}

	@Unique
	private boolean canBeFlippedTowards(BlockState state, Direction direction) {
		if(state.get(OPEN))
			return direction == state.get(FACING);
		else
			return direction == (state.get(HALF) == BlockHalf.BOTTOM ? Direction.UP : Direction.DOWN);
	}
}
