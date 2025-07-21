package com.fqf.mario_qua_mario_api.mixin.bappables;

import com.fqf.mario_qua_mario_api.MarioQuaMarioAPI;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import com.fqf.mario_qua_mario_api.interfaces.Bappable;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin implements Bappable {
	@Shadow @Final public static DirectionProperty FACING;

	@Shadow @Final public static BooleanProperty OPEN;

	@Shadow @Final public static EnumProperty<DoorHinge> HINGE;

	@Shadow protected abstract void playOpenCloseSound(@Nullable Entity entity, World world, BlockPos pos, boolean open);

	@Shadow public abstract boolean isOpen(BlockState state);

	@Override
	public void mqm$onBapped(IMarioData data, World world, BlockPos pos, BlockState blockState, Direction direction, int strength, BapResult result) {
		Bappable.super.mqm$onBapped(data, world, pos, blockState, direction, strength, result);

		switch(result) {
			case BUMP, BUMP_NO_POWER, BUMP_EMBRITTLE, BUMP_EMBRITTLE_NO_POWER, BREAK, BREAK_NO_POWER -> {
				Direction flipDir;

				if(blockState.get(OPEN)) {
					Direction facing = blockState.get(FACING);
					flipDir = switch(blockState.get(HINGE)) {
						case LEFT -> facing.rotateYClockwise();
						case RIGHT -> facing.rotateYCounterclockwise();
					};
				}
				else
					flipDir = blockState.get(FACING);

				if(direction == flipDir) {
					blockState = blockState.cycle(OPEN);
					world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
					this.playOpenCloseSound(data == null ? null : data.getMario(), world, pos, blockState.get(OPEN));
					world.emitGameEvent(data == null ? null : data.getMario(), this.isOpen(blockState) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
				}

//				MarioQuaMarioAPI.LOGGER.info("Door bap test:\n\t- Open: {}\n\t- Facing: {}\n\t- Hinge: {}\n\t- Bapdir: {}\n\t- Flipdir: {}",
//						blockState.get(OPEN), blockState.get(FACING), blockState.get(HINGE), direction, flipDir);
			}
		}
	}
}
