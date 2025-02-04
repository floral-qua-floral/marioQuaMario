package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import com.fqf.mario_qua_mario.util.MarioVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class DoubleJump extends Jump implements AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("double_jump");
	}

	private static LimbAnimation makeArmAnimation(int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.roll += progress * 70 * factor;
			arrangement.pitch += Easing.BACK_IN.ease((1 - progress)) * 26;
			arrangement.x += progress * -1.345F * factor;
			arrangement.y += Easing.BACK_OUT.ease(progress, 1.1F, -2.333F);
		});
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				(data, rightArmBusy, leftArmBusy, headRelativeYaw) -> data.getMario().getRandom().nextBoolean(),
				new ProgressHandler(
						(data, ticksPassed) ->
								Easing.EXPO_IN_OUT.ease(Easing.QUAD_IN.ease(Easing.clampedRangeToProgress(data.getYVel(), 0.87F, -0.85F)))
				),
				null,
				new BodyPartAnimation((data, arrangement, progress) ->
						arrangement.pitch += MathHelper.lerp(progress, -13, 27.5F)),
				new BodyPartAnimation((data, arrangement, progress) ->
						arrangement.yaw += progress * -10),
				makeArmAnimation(1),
				makeArmAnimation(-1),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.pitch += MathHelper.lerp(progress, 20, 9.1F);
					arrangement.z -= progress * 4.25F;
					arrangement.y -= progress * 4.5F;
				}),
				new LimbAnimation(false, (data, arrangement, progress) ->
						arrangement.pitch += MathHelper.lerp(progress, 20, -9.5F)),
				null
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}

	public static final CharaStat DOUBLE_JUMP_VEL = new CharaStat(0.939, JUMP_VELOCITY);
	public static final CharaStat DOUBLE_JUMP_ADDEND = new CharaStat(0.3, JUMP_VELOCITY);
	public static CharaStat DOUBLE_JUMP_SPEED_THRESHOLD = new CharaStat(0, WALKING, FORWARD, THRESHOLD);

	public static final TransitionDefinition TRIPLE_JUMPABLE_LANDING = Fall.LANDING.variate(
			null, null, null,
			data -> MarioVars.get(data).canTripleJumpTicks = 3,
			null
	);

	@Override protected double getJumpCapThreshold() {
		return 0.285;
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				TRIPLE_JUMPABLE_LANDING
		);
	}

	private TransitionInjectionDefinition makeInjection(Identifier injectNearTransitionsTo) {
		return new TransitionInjectionDefinition(
				TransitionInjectionDefinition.InjectionPlacement.BEFORE,
				injectNearTransitionsTo,
				TransitionInjectionDefinition.ActionCategory.GROUNDED,
				(nearbyTransition, castableHelper) -> nearbyTransition.variate(
						this.getID(),
						data ->
								MarioVars.get(data).canDoubleJumpTicks > 0
								&& data.getForwardVel() >= DOUBLE_JUMP_SPEED_THRESHOLD.get(data)
								&& nearbyTransition.evaluator().shouldTransition(data),
						null,
						data -> ((GroundedActionDefinition.GroundedActionHelper) castableHelper)
									.performJump(data, DOUBLE_JUMP_VEL, DOUBLE_JUMP_ADDEND),
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice("double_jump", seed);
						}
				)
		);
	}
	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
			this.makeInjection(MarioQuaMarioContent.makeID("jump")),
			this.makeInjection(MarioQuaMarioContent.makeID("p_jump"))
		);
	}
}
