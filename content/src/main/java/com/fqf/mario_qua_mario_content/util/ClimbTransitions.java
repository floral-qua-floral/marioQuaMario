package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.generic.ClimbPole;
import com.fqf.mario_qua_mario_content.actions.wallbound.ClimbIntangibleDirectional;
import com.fqf.mario_qua_mario_content.actions.wallbound.ClimbWall;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClimbTransitions {
	public static boolean canClimbBlock(BlockState state, Direction direction) {
		if(direction.getAxis().isHorizontal() && canClimbBlock(state)) {
			Optional<Direction> facing = state.getOrEmpty(HorizontalFacingBlock.FACING);
			if(facing.isPresent())
				return facing.get().getAxis() == direction.getAxis(); // can climb the back face of ladders because why not

			if(state.isIn(MQMContentTags.CLIMBABLE_PANES))
				return MultifaceGrowthBlock.hasDirection(state, direction.rotateYClockwise())
						|| MultifaceGrowthBlock.hasDirection(state, direction.rotateYCounterclockwise());

			return true;
		}
		return false;
	}
	private static boolean isIntangibleClimbable(PlayerEntity mario, BlockPos pos, BlockState state) {
		return canClimbBlock(state) && state.getCollisionShape(mario.getWorld(), pos, ShapeContext.of(mario)).isEmpty();
	}
	private static boolean canClimbBlock(BlockState state) {
		return state.isIn(MQMContentTags.CLIMBABLE) && !state.isOf(Blocks.SCAFFOLDING);
	}

	private static boolean testBackFaceClimbability(PlayerEntity mario, BlockPos pos, Direction inDirection) {
		BlockPos adjacentPos = pos.offset(inDirection);
		BlockState adjacentState = mario.getWorld().getBlockState(adjacentPos);

		if(!isIntangibleClimbable(mario, adjacentPos, adjacentState)) return false;

		Optional<Direction> facing = adjacentState.getOrEmpty(HorizontalFacingBlock.FACING);
		return facing.map(direction -> direction.equals(inDirection.getOpposite())).orElseGet(
				() -> MultifaceGrowthBlock.hasDirection(adjacentState, inDirection.getOpposite()));

	}
	private static Direction[] getDirections(PlayerEntity mario) {
		return new Direction[]{
				Direction.SOUTH,
				Direction.EAST,
				Direction.NORTH,
				Direction.WEST
		};
	}
	public static @Nullable Direction getIntangibleClimbableDirectionality(PlayerEntity mario, BlockPos pos, BlockState state) {
		Direction[] directions = getDirections(mario);

		if(isIntangibleClimbable(mario, pos, state)) {
			Optional<Direction> facing = state.getOrEmpty(HorizontalFacingBlock.FACING);
			if (facing.isPresent()) return facing.get().getOpposite();

			// this is dumb
			if(MultifaceGrowthBlock.hasDirection(state, directions[0])) return directions[0];
			if(MultifaceGrowthBlock.hasDirection(state, directions[1])) return directions[1];
			if(MultifaceGrowthBlock.hasDirection(state, directions[2])) return directions[2];
			if(MultifaceGrowthBlock.hasDirection(state, directions[3])) return directions[3];
		}

		if(testBackFaceClimbability(mario, pos, directions[0])) return directions[0];
		if(testBackFaceClimbability(mario, pos, directions[1])) return directions[1];
		if(testBackFaceClimbability(mario, pos, directions[2])) return directions[2];
		if(testBackFaceClimbability(mario, pos, directions[3])) return directions[3];

		return null;
	}

	public static boolean verifyIntangibleDirectionalClimbingLegality(PlayerEntity mario, BlockPos pos, Direction direction, boolean allowBackFace) {
		BlockState state = mario.getWorld().getBlockState(pos);
		if(isIntangibleClimbable(mario, pos, state)) {
			Optional<Direction> facing = state.getOrEmpty(HorizontalFacingBlock.FACING);
			if (facing.isPresent()) return facing.get().equals(direction.getOpposite());

			if(MultifaceGrowthBlock.hasDirection(state, direction)) return true;
		}
		return allowBackFace && testBackFaceClimbability(mario, pos, direction);
	}

	public static boolean isNonDirectional(BlockState state) {
		return !(state.contains(HorizontalFacingBlock.FACING)
				|| state.contains(MultifaceGrowthBlock.getProperty(Direction.SOUTH))
				|| state.contains(MultifaceGrowthBlock.getProperty(Direction.EAST))
				|| state.contains(MultifaceGrowthBlock.getProperty(Direction.NORTH))
				|| state.contains(MultifaceGrowthBlock.getProperty(Direction.WEST)));
	}

	public static boolean isNonSolidClimbable(IMarioReadableMotionData data, BlockPos pos, BlockState state, boolean directionality) {
		PlayerEntity mario = data.getMario();
		boolean acceptableDirectionality;
		if(directionality) {
			acceptableDirectionality = getIntangibleClimbableDirectionality(mario, pos, state) != null;
		}
		else {
			acceptableDirectionality = isIntangibleClimbable(mario, pos, state) && isNonDirectional(state);
		}
		return acceptableDirectionality;
	}

	public static boolean inNonSolidClimbable(IMarioReadableMotionData data, boolean directionality) {
		return isNonSolidClimbable(data, data.getMario().getBlockPos(), data.getMario().getBlockStateAtPos(), directionality);
	}

	private static boolean tryingToStartClimbingIntangible(IMarioReadableMotionData data) {
		if(data.getInputs().JUMP.isPressed()) return true;
		return MarioQuaMarioContent.CONFIG.doAutoLadder() && data.getRecordedCollisions().collidedHorizontally();
	}

	private static BlockPos getClimbingBlockPos(IMarioClientData data) {
		PlayerEntity mario = data.getMario();
		BlockPos pos = mario.getBlockPos();
		if(isIntangibleClimbable(mario, pos, mario.getBlockStateAtPos()))
			return pos;

		Direction[] directions = getDirections(mario);
		if(testBackFaceClimbability(mario, pos, directions[0])) return pos.offset(directions[0]);
		if(testBackFaceClimbability(mario, pos, directions[1])) return pos.offset(directions[1]);
		if(testBackFaceClimbability(mario, pos, directions[2])) return pos.offset(directions[2]);
		return pos.offset(directions[3]); // whatever!!
	}
	public static void playGrabIntangibleSound(IMarioClientData data, long seed) {
		BlockSoundGroup group = data.getMario().getWorld().getBlockState(getClimbingBlockPos(data)).getSoundGroup();
		data.playSound(group.getStepSound(), group.getPitch(), 1, seed);
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
					data.voice(Voicelines.DUCK_JUMP, seed);
					playGrabIntangibleSound(data, seed);
				}
		);
	}

	public static final CharaStat CLIMB_JUMP_UP_VEL = new CharaStat(1, StatCategory.JUMP_VELOCITY);

	public static final CharaStat CLIMB_JUMP_OFF_VEL = new CharaStat(0.8, StatCategory.JUMP_VELOCITY);
	public static final CharaStat CLIMB_JUMP_OFF_HORIZ_VEL = new CharaStat(0.8, StatCategory.BACKWARD);

	public static final TransitionDefinition CLIMB_NON_SOLID_DIRECTIONAL = makeNonSolidClimbingTransition(ClimbIntangibleDirectional.ID, true);
	public static final TransitionDefinition CLIMB_NON_SOLID_NON_DIRECTIONAL = makeNonSolidClimbingTransition(ClimbPole.ID, false);
	public static final TransitionDefinition CLIMB_SOLID = new TransitionDefinition(
			ClimbWall.ID,
			data -> data.getRecordedCollisions().getAnyMatch((collision, block) ->
					collision.direction().getAxis().isHorizontal() && canClimbBlock(block, collision.direction())) != null,
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setForwardStrafeVel(0, 0);
			},
			(data, isSelf, seed) -> {
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
