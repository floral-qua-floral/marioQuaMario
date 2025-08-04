package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.HelperGetter;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.util.RecordedCollisionSet;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.airborne.LavaBoost;
import com.fqf.mario_qua_mario_content.actions.generic.ClimbPole;
import com.fqf.mario_qua_mario_content.actions.generic.DebugSpinPitch;
import com.fqf.mario_qua_mario_content.actions.wallbound.ClimbIntangibleDirectional;
import com.fqf.mario_qua_mario_content.actions.wallbound.ClimbWall;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ClimbTransitions {
	private static final TagKey<Block> MARIO_CLIMBABLE =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of("mario_qua_mario:mario_climbable"));
	private static final TagKey<Block> MARIO_CLIMBABLE_PANES =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of("mario_qua_mario:mario_climbable_panes"));

	public static boolean canClimbBlock(BlockState state, Direction direction) {
		if(direction.getAxis().isHorizontal() && canClimbBlock(state)) {
			Optional<Direction> facing = state.getOrEmpty(HorizontalFacingBlock.FACING);
			if(facing.isPresent())
				return facing.get().getAxis() == direction.getAxis(); // can climb the back face of ladders because why not

			if(state.isIn(MARIO_CLIMBABLE_PANES))
				return MultifaceGrowthBlock.hasDirection(state, direction.rotateYClockwise())
						|| MultifaceGrowthBlock.hasDirection(state, direction.rotateYCounterclockwise());

			return true;
		}
		return false;
	}
	private static boolean canClimbBlock(BlockState state) {
		return state.isIn(MARIO_CLIMBABLE) && !state.isOf(Blocks.SCAFFOLDING);
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
				&& (hasDirectionality(state) == null) != directionality
				&& state.getCollisionShape(mario.getWorld(), mario.getBlockPos(), ShapeContext.of(mario)).isEmpty();
	}
	private static boolean tryingToStartClimbingIntangible(IMarioReadableMotionData data) {
		if(data.getInputs().JUMP.isPressed()) return true;
		return MarioQuaMarioContent.CONFIG.doAutoLadder() && data.getRecordedCollisions().collidedHorizontally();
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
					BlockSoundGroup group = data.getMario().getBlockStateAtPos().getSoundGroup();
					data.playSound(group.getStepSound(), group.getPitch(), 1, seed);
				}
		);
	}

	public static final CharaStat CLIMB_JUMP_UP_VEL = new CharaStat(1, StatCategory.JUMP_VELOCITY);

	public static final CharaStat CLIMB_JUMP_OFF_VEL = new CharaStat(0.8, StatCategory.JUMP_VELOCITY);
	public static final CharaStat CLIMB_JUMP_OFF_HORIZ_VEL = new CharaStat(0.8, StatCategory.BACKWARD);

	public static final TransitionDefinition CLIMB_NON_SOLID_DIRECTIONAL = makeNonSolidClimbingTransition(ClimbIntangibleDirectional.ID, true);
	public static final TransitionDefinition CLIMB_NON_SOLID_NON_DIRECTIONAL = makeNonSolidClimbingTransition(ClimbPole.ID, false);
	private static final RecordedCollisionSet.CollisionMatcher climbSolidMatcher = (collision, block) -> canClimbBlock(block, collision.direction());
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
