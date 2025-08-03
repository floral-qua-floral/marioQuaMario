package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.generic.ClimbPole;
import com.fqf.mario_qua_mario_content.actions.generic.DebugSpinPitch;
import com.fqf.mario_qua_mario_content.actions.wallbound.ClimbIntangibleDirectional;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClimbTransitions {
	private static boolean canClimbBlock(BlockState state) {
		return state.isIn(BlockTags.CLIMBABLE) && !state.isOf(Blocks.SCAFFOLDING);
	}
	public static @Nullable Direction hasDirectionality(BlockState state) {
		Optional<Direction> facing = state.getOrEmpty(HorizontalFacingBlock.FACING);
		if(facing.isPresent()) return facing.get().getOpposite();

		// this is dumb
		if(MultifaceGrowthBlock.hasDirection(state, Direction.SOUTH)) return Direction.SOUTH;
		if(MultifaceGrowthBlock.hasDirection(state, Direction.EAST)) return Direction.EAST;
		if(MultifaceGrowthBlock.hasDirection(state, Direction.NORTH)) return Direction.NORTH;
		if(MultifaceGrowthBlock.hasDirection(state, Direction.WEST)) return Direction.WEST;

		return null;
	}
	public static boolean inNonSolidClimbable(IMarioReadableMotionData data, boolean directionality) {
		PlayerEntity mario = data.getMario();
		BlockState state = mario.getBlockStateAtPos();
		return
				canClimbBlock(state)
				&& (hasDirectionality(state) == null) != directionality;
//				&& state.getCollisionShape(mario.getWorld(), mario.getBlockPos(), ShapeContext.of(mario)).isEmpty(); // TODO: Uncomment
	}
	private static boolean tryingToStartClimbingIntangible(IMarioReadableMotionData data) {
		if(data.getInputs().JUMP.isPressed()) return true;
		return MarioQuaMarioContent.CONFIG.doAutoLadder() && data.getLastTickCollisions().collidedHorizontally();
	}
	private static TransitionDefinition makeNonSolidClimbingTransition(Identifier targetActionID, boolean directionality) {
		return new TransitionDefinition(
				targetActionID,
				data -> inNonSolidClimbable(data, directionality) && (data.isServer() || tryingToStartClimbingIntangible(data)),
				EvaluatorEnvironment.CLIENT_CHECKED,
				data -> {
					data.centerLaterally();
					data.setForwardStrafeVel(0, 0);
				},
				(data, isSelf, seed) -> {
					data.playSound(MarioContentSFX.YOSHI, seed);
					data.voice(Voicelines.DUCK_JUMP, seed);
				}
		);
	}

	public static final CharaStat CLIMB_JUMP_UP_VEL = new CharaStat(1, StatCategory.JUMP_VELOCITY);

	public static final CharaStat CLIMB_JUMP_OFF_VEL = new CharaStat(0.8, StatCategory.JUMP_VELOCITY);
	public static final CharaStat CLIMB_JUMP_OFF_HORIZ_VEL = new CharaStat(0.8, StatCategory.BACKWARD);

	public static final TransitionDefinition CLIMB_NON_SOLID_DIRECTIONAL = makeNonSolidClimbingTransition(ClimbIntangibleDirectional.ID, true);
	public static final TransitionDefinition CLIMB_NON_SOLID_NON_DIRECTIONAL = makeNonSolidClimbingTransition(ClimbPole.ID, false);
	public static final TransitionDefinition CLIMB_SOLID = new TransitionDefinition(
			DebugSpinPitch.ID,
			data -> {
				return false;
			},
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {},
			(data, isSelf, seed) -> {
				data.playSound(MarioContentSFX.YOSHI, seed);
				data.voice(Voicelines.DUCK_JUMP, seed);
			}
	);

	public static float yawOf(@Nullable Direction direction) {
		if(direction == null) return Float.NaN;
		return switch(direction) {
			case NORTH -> 180;
			case SOUTH -> 0;
			case WEST -> 90;
			case EAST -> -90;
			default -> throw new IllegalStateException("Direction has no yaw: " + direction + " :(");
		};
	}
}
