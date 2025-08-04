package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.HelperGetter;
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
import net.minecraft.util.math.Vec3d;
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

	@Override public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.TOWARDS;
	}
	@Override public float getHeadYawRange() {
		return 360;
	}

	private static final boolean SIDLE_ENABLED = false;

	protected static float currentBlockYaw(IMarioReadableMotionData data) {
		return ClimbTransitions.yawOf(ClimbTransitions.hasDirectionality(data.getMario().getBlockStateAtPos()));
	}
	private static boolean isInAcceptableBlock(IMarioReadableMotionData data, WallInfo wall) {
		return ClimbTransitions.inNonSolidClimbable(data, true) && wall.getWallYaw() == currentBlockYaw(data);
	}

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return currentBlockYaw(data);
	}
	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ClimbVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public boolean checkLegality(IMarioReadableMotionData data, WallInfo wall) {
		return isInAcceptableBlock(data, wall);
	}

	@Override public void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper) {
		data.getMario().fallDistance = 0;
		double forwardInput = wall.getTowardsWallInput() * (data.getInputs().DUCK.isHeld() ? 0.3 : 1);
		data.setYVel(forwardInput * ClimbPole.CLIMB_SPEED.get(data));
		data.getVars(ClimbVars.class).progress += (float) forwardInput;
		helper.setSidleVel(data, SIDLE_ENABLED && Math.abs(wall.getSidleInput()) > 0.15 ? wall.getSidleInput() * 0.1 : 0);
	}

	private static final WallboundActionHelper HELPER = HelperGetter.getWallboundActionHelper();
	public static final TransitionDefinition BACKFLIP_OFF_LADDER = new TransitionDefinition(
			Backflip.ID,
			data -> HELPER.getWallInfo(data) != null
					&& Objects.requireNonNull(HELPER.getWallInfo(data)).getTowardsWallInput() < -0.45
					&& data.getInputs().JUMP.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setYVel(Backflip.BACKFLIP_VEL.get(data));
				HELPER.setTowardsWallVel(data, Backflip.BACKFLIP_BACKWARDS_SPEED.get(data));
			},
			(data, isSelf, seed) -> data.playJumpSound(seed)
	);
	public static final TransitionDefinition DROP_OFF_LADDER = new TransitionDefinition(
			Fall.ID,
			data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				HELPER.setTowardsWallVel(data, 0);
				data.getInputs().DUCK.isPressed(); // Unbuffer Duck
			},
			null
	);
	public static final TransitionDefinition JUMP_OFF_LADDER = new TransitionDefinition(
			Jump.ID,
			data -> data.getInputs().JUMP.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				HELPER.setTowardsWallVel(data, 0);
				data.setYVel(Jump.JUMP_VEL.get(data));
			},
			(data, isSelf, seed) -> data.playJumpSound(seed)
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						ClimbIntangibleSideHang.ID,
						data -> Math.abs(helper.getWallInfo(data).getYawDeviation()) > 99,
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.setYVel(Math.min(0, data.getYVel())),
						null
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper) {
		return List.of(
				BACKFLIP_OFF_LADDER,
				DROP_OFF_LADDER,
				JUMP_OFF_LADDER
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SpecialFall.ID,
						data -> !isInAcceptableBlock(data, helper.getWallInfo(data)),
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
