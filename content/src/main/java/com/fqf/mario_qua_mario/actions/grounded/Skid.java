package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.airborne.*;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class Skid implements GroundedActionDefinition {
	public static Identifier ID = MarioQuaMarioContent.makeID("skid");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> 1),
				new EntireBodyAnimation(0.3F, (data, arrangement, progress) -> {
					arrangement.y -= 4;
					arrangement.setAngles(
							0,
							progress * 42.5F,
							progress * 17.5F
					);
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.roll -= progress * 15;
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.addAngles(
							progress * -32,
							progress * -35,
							progress * 80
					);
				}),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.addAngles(
							-45,
							0,
							-30
					);
				}),

				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.addAngles(
							progress * -57.5F,
							progress * 45F,
							progress * -20
					);
				}),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.addPos(
							progress * 0,
							progress * -4.1F,
							progress * -3.9F
					);
					arrangement.addAngles(
							progress * 5,
							progress * 15,
							progress * 0
					);
				}),

				new LimbAnimation(false, (data, arrangement, progress) -> {

				})
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SKIDDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static final CharaStat SKID_THRESHOLD = new CharaStat(0.285, RUNNING, THRESHOLD);
	public static final CharaStat SKID_BOOST = new CharaStat(-0.15);

	public static final CharaStat SKID_DRAG = new CharaStat(0.185, RUNNING, DRAG);
	public static final CharaStat SKID_DRAG_MIN = new CharaStat(0.045, RUNNING, DRAG);
	public static final CharaStat SKID_REDIRECTION = new CharaStat(4.5, RUNNING, REDIRECTION);

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
				data, SKID_DRAG, SKID_DRAG_MIN,
				-data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SKID_REDIRECTION
		);
	}

	public static final TransitionDefinition SKID = new TransitionDefinition(
			ID,
			data -> data.getInputs().getForwardInput() < -0.675 && data.getForwardVel() > SKID_THRESHOLD.get(data),
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("sub_walk"),
						data -> data.getHorizVelSquared() == 0 || data.getInputs().getForwardInput() >= 0,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				Jump.makeJumpTransition(helper)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL
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
