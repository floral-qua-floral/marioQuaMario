package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.StandUpWithKneeAnimation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class BonkGroundBackward implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("bonk_ground_backward");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		boolean isRight = factor == 1;
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					22.5F,
					isRight ? 0 : -110,
					0
			);
			arrangement.yaw += helper.interpolateKeyframes(progress,
					factor * -20,
					isRight ? 0 : 37.5F,
					isRight ? 0 : Easing.QUART_IN.ease(progress - 1, 90F, 0)
			);
			arrangement.roll += helper.interpolateKeyframes(progress,
					0,
					isRight ? 0 : -90,
					0
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					0,
					isRight ? 1 : 0,
					0
			);
			arrangement.z += helper.interpolateKeyframes(progress,
					2,
					isRight ? 0 : 1,
					0
			);
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		boolean isRight = factor == 1;
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					-90,
					isRight ? -79 : 15,
					0
			);
			arrangement.yaw += helper.interpolateKeyframes(progress,
					factor * 5,
					isRight ? factor * 10 : 0,
					0
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					-1.25F,
					isRight ? -1.55F : -8.5F,
					0
			);
			arrangement.z += helper.interpolateKeyframes(progress,
					1.5F,
					isRight ? 3 : -3,
					0
			);
		});
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return StandUpWithKneeAnimation.getAnimation(
				helper, (data, ticksPassed) -> data.getVars(ActionTimerVars.class).actionTimer / (float) STANDUP_TICKS,
				1.75F, 10,
				22.5F, -20, 0, 2,
				-90, 5, 1.5F,
				-79, 10, -1.55F, 3
		);
//		return new PlayermodelAnimation(
//				null,
//				new ProgressHandler((data, ticksPassed) -> 2 * Easing.SINE_IN_OUT.ease(Math.min(1, data.getVars(ActionTimerVars.class).actionTimer / (float) STANDUP_TICKS))),
//				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
//					arrangement.y += helper.interpolateKeyframes(progress,
//							-9.75F,
//							-8.5F,
//							0
//					);
//					arrangement.z += helper.interpolateKeyframes(progress,
//							1.75F,
//							0,
//							0
//					);
//				}),
//				new BodyPartAnimation((data, arrangement, progress) -> {
//
//				}),
//				new BodyPartAnimation((data, arrangement, progress) -> {
//					arrangement.pitch += helper.interpolateKeyframes(progress,
//							10,
//							25,
//							0
//					);
//				}),
//				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
//				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
//				null
//		);
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING;
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

	public static final CharaStat BONK_DRAG = new CharaStat(0.185, RUNNING, DRAG);
	public static final CharaStat BONK_DRAG_MIN = new CharaStat(0.045, RUNNING, DRAG);

	public static final int STANDUP_TICKS = 8;

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		if(data.getHorizVelSquared() == 0) data.getVars(ActionTimerVars.class).actionTimer++;
		else data.getVars(ActionTimerVars.class).actionTimer = 0;
		helper.applyDrag(
				data, BONK_DRAG, BONK_DRAG_MIN,
				-data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				CharaStat.ZERO
		);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SubWalk.ID,
						data -> data.getVars(ActionTimerVars.class).actionTimer > STANDUP_TICKS,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL.variate(
						BonkAir.ID,
						data -> Fall.FALL.evaluator().shouldTransition(data) && data.getVars(ActionTimerVars.class).actionTimer == 0,
						EvaluatorEnvironment.CLIENT_ONLY,
						null,
						null
				),
				Fall.FALL,
				UnderwaterWalk.SUBMERGE
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
