package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.HelperGetter;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.block.BlockState;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ClimbVars {
	public float progress;
	private boolean ticked;

	public void clientTick(IMarioClientData data) {
		if(!ticked) {
			ticked = true;
			WallboundActionDefinition.WallInfo wall = HelperGetter.getWallboundActionHelper().getWallInfo((IMarioReadableMotionData) data);
			World world = data.getMario().getWorld();
			BlockSoundGroup wallSoundGroup = null;
			for(BlockPos block : wall.getWallBlocks(1)) {
				BlockState blockState = world.getBlockState(block);
				if(ClimbTransitions.canClimbBlock(blockState, Direction.fromRotation(wall.getWallYaw()))) {
					wallSoundGroup = blockState.getSoundGroup();
					break;
				}
			}
			if(wallSoundGroup == null) {
				MarioQuaMarioContent.LOGGER.info("Tried to play climb start sound, but no climbable blocks were found?!");
				return;
			}
			data.playSound(wallSoundGroup.getStepSound(), wallSoundGroup.pitch, 1, data.getMario().getRandom().nextLong());
		}
	}
}
