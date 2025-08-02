package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.*;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_content.actions.airborne.Backflip;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.Jump;
import com.fqf.mario_qua_mario_content.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario_content.actions.generic.ClimbPole;
import com.fqf.mario_qua_mario_content.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
import com.fqf.mario_qua_mario_content.util.ClimbVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClimbIntangibleDirectional implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_intangible_directional");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return ClimbPole.makeAnimation(0);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	private static final boolean SIDLE_ENABLED = false;

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return ClimbTransitions.yawOf(ClimbTransitions.hasDirectionality(data.getMario().getBlockStateAtPos()));
	}
	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ClimbVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public boolean checkServerSidedLegality(IMarioReadableMotionData data, WallInfo wall) {
		MarioQuaMarioContent.LOGGER.info("Checking server-side legality of ClimbIntangibleDirectional! Uwu!");
		return true;
	}

	@Override public void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper) {
		data.getMario().fallDistance = 0;
//		helper.assignWallDirection(data, data.getVars(ClimbWallVars.class).WALL_DIRECTION);
		data.goTo(data.getMario().getBlockPos().toCenterPos().withAxis(Direction.Axis.Y, data.getMario().getY()));
		wall = helper.getWallInfo(data);
		if(wall == null) {
			data.setYVel(0);
		}
		else {
			double forwardInput = wall.getTowardsWallInput() * (data.getInputs().DUCK.isHeld() ? 0.3 : 1);
			data.setYVel(wall.getTowardsWallInput() * ClimbPole.CLIMB_SPEED.get(data));
			data.getVars(ClimbVars.class).progress += (float) forwardInput;
			helper.setSidleVel(data, SIDLE_ENABLED && Math.abs(wall.getSidleInput()) > 0.15 ? wall.getSidleInput() * 0.1 : 0);
		}
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						Backflip.ID,
						data -> helper.getWallInfo(data) != null
								&& Objects.requireNonNull(helper.getWallInfo(data)).getTowardsWallInput() < 0
								&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							data.setYVel(Backflip.BACKFLIP_VEL.get(data));
							data.setForwardVel(Backflip.BACKFLIP_BACKWARDS_SPEED.get(data));
						},
						(data, isSelf, seed) -> data.playJumpSound(seed)
				),
				new TransitionDefinition(
						Fall.ID,
						data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.getInputs().DUCK.isPressed(), // Unbuffer Duck
						null
				),
				new TransitionDefinition(
						Jump.ID,
						data -> data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.setYVel(Jump.JUMP_VEL.get(data)),
						(data, isSelf, seed) -> data.playJumpSound(seed)
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SpecialFall.ID,
						data -> {
							MarioQuaMarioContent.LOGGER.info("In non-solid directional climbable: {}", ClimbTransitions.inNonSolidClimbable(data, true));
							MarioQuaMarioContent.LOGGER.info("Directionality: {}", ClimbTransitions.hasDirectionality(data.getMario().getBlockStateAtPos()));
							MarioQuaMarioContent.LOGGER.info("Yaw of directionality: {}", ClimbTransitions.yawOf(ClimbTransitions.hasDirectionality(data.getMario().getBlockStateAtPos())));
							MarioQuaMarioContent.LOGGER.info("Yaw of current wall: {}", helper.getWallInfo(data).getWallYaw());
							return !ClimbTransitions.inNonSolidClimbable(data, true)
									|| helper.getWallInfo(data).getWallYaw() != ClimbTransitions.yawOf(ClimbTransitions.hasDirectionality(data.getMario().getBlockStateAtPos()));
						},
						EvaluatorEnvironment.COMMON
				),
				new TransitionDefinition(
						SubWalk.ID,
						data -> data.getMario().isOnGround(),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
