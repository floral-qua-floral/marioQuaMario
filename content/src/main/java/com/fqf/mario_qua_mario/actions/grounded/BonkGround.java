package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.CfaMarioAddon;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario.util.StandUpWithKneeAnimation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.DRAG;
import static com.fqf.charaformact_api.util.StatCategory.RUNNING;

public class BonkGround implements GroundedActionDefinition {
	public static final Identifier BACKWARD_ID = MarioQuaMario.makeID("bonk_ground_backward");
	public static final Identifier FORWARD_ID = MarioQuaMario.makeID("bonk_ground_forward");

	@Override
	public float defineHitboxEyeHeight() {
		return Math.min(GroundedActionDefinition.super.defineHitboxEyeHeight(), this.defineHitboxHeight() / 2 + 0.2F);
	}

	private final boolean IS_BACKWARD;

	public static final float STANDUP_TICKS = 8;
	protected static final StandUpWithKneeAnimation.ProgressCalculator PROGRESS_CALCULATOR = (data, animationTime) ->
			2 * Easing.SINE_IN_OUT.ease(Math.min(1, data.get(CfaMarioAddon.ACTION_TIMER).intValue() / STANDUP_TICKS));

	public BonkGround(boolean isBackward) {
		this.IS_BACKWARD = isBackward;
	}

	public static AnimationDefinition makeAnimation(StandUpWithKneeAnimation.ProgressCalculator progressCalculator) {
		return StandUpWithKneeAnimation.makeAnimation(
				progressCalculator,
				1.75F, 10,
				22.5F, -20, 0, 2,
				-90, 5, 1.5F,
				-79, 10, -1.55F, 3
		);
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return this.IS_BACKWARD ? makeAnimation(PROGRESS_CALCULATOR) : StandUpWithKneeAnimation.makeAnimation(
				PROGRESS_CALCULATOR,
				-3.25F, 40,
				-80, -80, 90, 1.25F,
				87.5F, 0, 1.5F,
				90, 0, 0, -2
		);
	}

	@Override public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.addAll(ActionFlag.SLIDING);
		builder.add(ActionFlag.PROHIBIT_SPRINT, ActionFlag.CROUCHING, ActionFlag.NO_LEDGE_CLIP);
	}

	@Override
	public void clientTick(CfaClientData data, boolean isSelf) {
		DuckSlide.slideSound(data);
		Vec3d deltaPos = Skid.getDeltaPos(data, false);
		if(deltaPos.horizontalLengthSquared() != 0)
			Skid.spawnParticles(data, 1, deltaPos, deltaPos.horizontalLength(), 0.1);
	}

	public static final CfaStat BONK_DRAG = new CfaStat(0.185, RUNNING, DRAG);
	public static final CfaStat BONK_DRAG_MIN = new CfaStat(0.045, RUNNING, DRAG);

	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		if(data.getHorizVelSquared() == 0) data.get(CfaMarioAddon.ACTION_TIMER).increment();
		else data.get(CfaMarioAddon.ACTION_TIMER).setValue(0);
		helper.applyDrag(
				data, BONK_DRAG, BONK_DRAG_MIN,
				-data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				CfaStat.ZERO
		);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				SubWalk.ID,
				data -> data.get(CfaMarioAddon.ACTION_TIMER).intValue() > STANDUP_TICKS,
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Fall.FALL.variate(
						BonkAir.ID,
						data -> Fall.FALL.evaluator().test(data) && data.get(CfaMarioAddon.ACTION_TIMER).intValue() == 0,
						EvaluatorEnvironment.CLIENT_ONLY,
						null, null,
						null
				),
				Fall.FALL,
				UnderwaterWalk.SUBMERGE
		);
	}
}
