package com.fqf.mario_qua_mario_content.actions.generic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class DebugSideTurn implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("debug_side_turn");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	public static final PlayermodelAnimation ANIMATION = new PlayermodelAnimation(
			null,
			new ProgressHandler(null, (data, prevAnimationID) -> true, (data, ticksPassed) -> Math.min(ticksPassed / 40F, 1)),
			new EntireBodyAnimation(0.5F, false,
					(data, arrangement, progress) -> arrangement.yaw -= MathHelper.lerp(Math.max(progress * 2 - 1, 0), -90, 0)),
			null,
			new BodyPartAnimation((data, arrangement, progress) -> {
				float useProgress = progress > 0.5F ? 1 - Math.max(progress * 2 - 1, 0) : Math.min(progress * 2, 1);
				arrangement.yaw -= MathHelper.lerp(useProgress, 0, -90);
			}),
			new LimbAnimation(false, (data, arrangement, progress) -> arrangement.roll += 90),
			new LimbAnimation(false, (data, arrangement, progress) -> arrangement.roll -= 90),

			new LimbAnimation(false, null), new LimbAnimation(false, null),
			new LimbAnimation(false, null)
	);

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return ANIMATION;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
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

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		if(data.getVars(ActionTimerVars.class).actionTimer++ == 1) {
			data.instantVisualRotate(90, true);
		}
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public boolean travelHook(IMarioTravelData data) {
		return true;
	}

	public static final TransitionDefinition SIDE_TURN = new TransitionDefinition(
			ID,
			data -> data.getInputs().SPIN.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.getMario().setYaw(data.getMario().getYaw() + 90);
			},
			(data, isSelf, seed) -> {
				data.forceBodyAlignment(true);
				data.instantVisualRotate(90, true);
			}
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of(
				SIDE_TURN
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions() {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions() {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
