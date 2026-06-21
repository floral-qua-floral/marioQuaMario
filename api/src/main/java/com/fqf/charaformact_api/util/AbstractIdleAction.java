package com.fqf.charaformact_api.util;

import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractIdleAction implements GroundedActionDefinition {
	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {

	}

	public abstract @NotNull Identifier getWakeupID();
	protected final @NotNull Identifier WAKEUP_ID = this.getWakeupID();

	public static boolean isIdle(CfaReadableMotionData data) {
		return MathHelper.approximatelyEquals(data.getForwardVel(), 0)
				&& MathHelper.approximatelyEquals(data.getStrafeVel(), 0)
				&& MathHelper.approximatelyEquals(data.getInputs().getForwardInput(), 0)
				&& MathHelper.approximatelyEquals(data.getInputs().getStrafeInput(), 0)
				&& !data.getInputs().DUCK.isHeld();
	}
	public final ActionTransitionDetails WAKEUP_TRANSITION = new ActionTransitionDetails(
			this.WAKEUP_ID,
			data -> !isIdle(data),
			EvaluatorEnvironment.CLIENT_ONLY
	);
//	public final ActionTransitionDetails IDLE_TRANSITION = new ActionTransitionDetails(
//			this.defineID(),
//			AbstractIdleAction::isIdle,
//			EvaluatorEnvironment.CLIENT_ONLY
//	);

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(WAKEUP_TRANSITION);
	}
}