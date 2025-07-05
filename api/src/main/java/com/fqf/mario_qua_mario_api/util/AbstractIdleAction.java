package com.fqf.mario_qua_mario_api.util;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class AbstractIdleAction implements GroundedActionDefinition {
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
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
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {

	}

	public abstract @NotNull Identifier getWakeupID();
	protected final @NotNull Identifier WAKEUP_ID = this.getWakeupID();

	public static boolean isIdle(IMarioReadableMotionData data) {
		return MathHelper.approximatelyEquals(data.getForwardVel(), 0)
				&& MathHelper.approximatelyEquals(data.getStrafeVel(), 0)
				&& MathHelper.approximatelyEquals(data.getInputs().getForwardInput(), 0)
				&& MathHelper.approximatelyEquals(data.getInputs().getStrafeInput(), 0)
				&& !data.getInputs().DUCK.isHeld();
	}
	public final TransitionDefinition WAKEUP_TRANSITION = new TransitionDefinition(
			this.WAKEUP_ID,
			data -> !isIdle(data),
			EvaluatorEnvironment.CLIENT_ONLY
	);
	public final TransitionDefinition IDLE_TRANSITION = new TransitionDefinition(
			this.getID(),
			AbstractIdleAction::isIdle,
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				WAKEUP_TRANSITION
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}