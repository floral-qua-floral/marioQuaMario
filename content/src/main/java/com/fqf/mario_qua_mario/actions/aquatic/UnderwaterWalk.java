package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.LavaBoost;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class UnderwaterWalk implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("underwater_walk");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	public static final float LEG_HEIGHT_OFFSET = -1.8F;

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
			(arrangement, data, animationTime, helper) -> {
				arrangement.y += LEG_HEIGHT_OFFSET;
			},
			(posture, data, animationTime, helper) -> {
				posture.TORSO.pitch += 15;

				helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
					arrangement.roll *= -1;
					arrangement.addAngles(
							-50,
							-10 - arrangement.pitch * 0.5F,
							60 - arrangement.pitch * 0.5F
					);
				});

				helper.symmetricallyAnimate(posture, posture.RIGHT_LEG, arrangement -> {
					arrangement.addPos(
							-0.4F,
							LEG_HEIGHT_OFFSET,
							-0.9F
					);
					arrangement.pitch *= 0.5F;
					arrangement.addAngles(
							17.5F,
							0,
							-1.9F
					);
				});
			}
		);
	}
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
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	public static CfaStat REDUCED_FORWARD_ACCEL = Submerged.FORWARD_SWIM_ACCEL.variateAndAddCategories(0.475, StatCategory.WALKING);
	public static CfaStat REDUCED_FORWARD_SPEED = Submerged.FORWARD_SWIM_SPEED.variateAndAddCategories(0.475, StatCategory.WALKING);
	public static CfaStat REDUCED_BACKWARD_ACCEL = Submerged.BACKWARD_SWIM_ACCEL.variateAndAddCategories(0.475, StatCategory.WALKING);
	public static CfaStat REDUCED_BACKWARD_SPEED = Submerged.BACKWARD_SWIM_SPEED.variateAndAddCategories(0.475, StatCategory.WALKING);
	public static CfaStat REDUCED_STRAFE_ACCEL = Submerged.STRAFE_SWIM_ACCEL.variateAndAddCategories(0.475, StatCategory.WALKING);
	public static CfaStat REDUCED_STRAFE_SPEED = Submerged.STRAFE_SWIM_SPEED.variateAndAddCategories(0.475, StatCategory.WALKING);

	public static CfaStat REDUCED_REDIRECTION = Submerged.SWIM_REDIRECTION.variateAndAddCategories(0.7, StatCategory.WALKING);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		Submerged.waterMove(data, helper);
		helper.aquaticAccel(
				data,
				REDUCED_FORWARD_ACCEL, REDUCED_FORWARD_SPEED,
				REDUCED_BACKWARD_ACCEL, REDUCED_BACKWARD_SPEED,
				REDUCED_STRAFE_ACCEL, REDUCED_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				REDUCED_REDIRECTION
		);
	}

	public static final TransitionDefinition SUBMERGE = new TransitionDefinition(
			ID,
			data -> data.getImmersionPercent() > 0.5 && (data.getActionID() != LavaBoost.ID || data.getYVel() < 0),
			EvaluatorEnvironment.COMMON
	);

	public static final TransitionDefinition EXIT_WATER = new TransitionDefinition(
			SubWalk.ID,
			data -> data.getImmersionPercent() < 0.5,
			EvaluatorEnvironment.COMMON
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of(
				DuckWaddle.DUCK.variate(
						UnderwaterDuck.ID,
						null, null,
						null,
						(data, isSelf, seed) -> data.playSound(MarioSFX.DUCK, seed)
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of(
				Swim.SWIM
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
				EXIT_WATER,
				Fall.FALL.variate(Submerged.ID,
						data -> Fall.FALL.evaluator().shouldTransition(data) && SUBMERGE.evaluator().shouldTransition(data))
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}