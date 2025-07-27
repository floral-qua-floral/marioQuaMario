package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.airborne.Backflip;
import com.fqf.mario_qua_mario_content.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario_content.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario_content.actions.airborne.LongJump;
import com.fqf.mario_qua_mario_content.actions.aquatic.UnderwaterDuck;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class DuckSlide implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("duck_slide");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return DuckWaddle.makeDuckAnimation(false, false);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.SLIP;
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

	public static final CharaStat SLIDE_THRESHOLD = new CharaStat(0.25, DUCKING, THRESHOLD);
	public static final CharaStat SLIDE_BOOST = new CharaStat(-0.15);

	public static final CharaStat SLIDE_DRAG = new CharaStat(0.04333, DUCKING, DRAG);
	public static final CharaStat SLIDE_DRAG_MIN = new CharaStat(0.01, DUCKING, DRAG);
	public static final CharaStat SLIDE_REDIRECTION = new CharaStat(4.0, DUCKING, REDIRECTION);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		data.getVars(ActionTimerVars.class).actionTimer++;
		helper.applyDrag(
				data, SLIDE_DRAG, SLIDE_DRAG_MIN,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SLIDE_REDIRECTION
		);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						DuckWaddle.ID,
						data -> data.getHorizVelSquared() == 0,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckWaddle.UNDUCK,
				Backflip.makeBackflipTransition(helper),
				new TransitionDefinition(
						LongJump.ID,
						data ->
								data.getInputs().getForwardInput() > 0.4
								&& data.getVars(ActionTimerVars.class).actionTimer < 5
								&& data.getForwardVel() > LongJump.LONG_JUMP_THRESHOLD.get(data)
								&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							helper.performJump(data, LongJump.LONG_JUMP_VEL, null);
							data.setForwardVel(data.getForwardVel() * 0.92 + 0.098);
						},
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(Voicelines.LONG_JUMP, seed);
						}
				),
				DuckJump.makeDuckJumpTransition(helper)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckFall.DUCK_FALL,
				UnderwaterDuck.DUCK_SUBMERGE
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						DuckWaddle.ID,
						ActionCategory.GROUNDED,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								DuckSlide.ID,
								data -> {
									double threshold = SLIDE_THRESHOLD.get(data);
									return data.getHorizVelSquared() > threshold * threshold
											&& nearbyTransition.evaluator().shouldTransition(data);
								},
								EvaluatorEnvironment.CLIENT_ONLY,
								null, null
						)
				),

				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						DuckWaddle.ID,
						ActionCategory.AIRBORNE,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								DuckSlide.ID,
								data -> {
									double threshold = SLIDE_THRESHOLD.get(data);
									return (data.isServer() || (data.getHorizVelSquared() > threshold * threshold))
											&& nearbyTransition.evaluator().shouldTransition(data);
								},
								EvaluatorEnvironment.CLIENT_CHECKED,
								null, null
						)
				)
		);
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
