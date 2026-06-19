package com.fqf.mario_qua_mario.actions.form;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.forms.Raccoon;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class TailStall extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("tail_stall");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	public static final float TAIL_WAGGLE_FREQUENCY = 1.1F;

	public static final AnimationDefinition.PostureMutator POSTURE_MUTATOR = (posture, data, animationTime, helper) -> {
		if(posture.TAIL != null) {
			float progress = animationTime * TAIL_WAGGLE_FREQUENCY;
			posture.TAIL.setAngles(
					MathHelper.sin(progress * 1.2F) * 38.3F,
					MathHelper.sin(progress * 0.6F) * 52,
					0
			);
		}
	};

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(posture, data, animationTime, helper) -> {
					float progress = animationTime * TAIL_WAGGLE_FREQUENCY;

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						arrangement.x -= sideFactor;
						arrangement.addAngles(
								sideFactor * MathHelper.cos(progress) * 11,
								0,
								100 * sideFactor + MathHelper.sin(progress) * 5
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) ->
							arrangement.pitch += sideFactor * MathHelper.cos(progress) * 70F);

					POSTURE_MUTATOR.mutatePosture(posture, data, animationTime, helper);
				}
		);
	}

	public static final CfaStat FALL_ACCEL = new CfaStat(-0.013775, NORMAL_GRAVITY, FORM);
	public static final CfaStat FALL_SPEED = new CfaStat(-0.445, TERMINAL_VELOCITY, FORM);

	public static void tailWaggleTick(CfaClientData data) {
		if(data.retrieveStateData(ActionTimerVars.class).actionTimer++ % 4 == 0)
			data.playSound(MarioSFX.TAIL_FLY, 1F, 0.1F, data.getPlayer().getRandom().nextLong());
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		tailWaggleTick(data);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		drift(data, helper);
		if(data.getYVel() > FALL_SPEED.get(data)) data.getPlayer().fallDistance = 0;
	}

	protected static final TransitionDefinition END_STALLING = new TransitionDefinition(
			Fall.ID,
			data -> !data.getInputs().JUMP.isHeld() || data.getYVel() > 0,
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SpecialFall.ID, // special fall coming in CLUTCH!
						data -> !data.hasPower(Powers.TAIL_STALL),
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				GroundPoundFlip.GROUND_POUND,
				END_STALLING
		);
	}

	public static final CfaStat STALL_THRESHOLD = new CfaStat(-0.31, THRESHOLD, FORM);
	private static final TransitionDefinition STALL_TRANSITION = new TransitionDefinition(
			TailStall.ID,
			data ->
					data.hasPower(Powers.TAIL_STALL)
					&& !data.getPlayer().isInSneakingPose()
					&& (data.isServer() || (
							data.getYVel() < STALL_THRESHOLD.get(data)
							&& data.getInputs().JUMP.isHeld()
					)),
			EvaluatorEnvironment.CLIENT_CHECKED,
			data -> {
				Raccoon.RaccoonVars vars = data.retrieveStateData(Raccoon.RaccoonVars.class);

				// If Mario hasn't initiated tail-stalling yet, then set it to its initial value
				if(vars.stallStartVel == null)
					vars.stallStartVel = STALL_THRESHOLD.get(data) * 0.2;
				// If he has, and his current Y velocity is actually higher than the decayed value, then set
				// the decayed value to his current Y velocity instead.
				else if(data.getYVel() > vars.stallStartVel)
					vars.stallStartVel = data.getYVel();

				data.setYVel(vars.stallStartVel);
			},
			null
	);
	protected static final Identifier DUCK_STALL_ID = MarioQuaMario.makeID("tail_stall_duck");
	private static final TransitionDefinition DUCK_STALL_TRANSITION = STALL_TRANSITION.variate(
			DUCK_STALL_ID,
			data ->
					data.hasPower(Powers.TAIL_STALL)
					&& data.getPlayer().isInSneakingPose()
					&& (data.isServer() || (
							data.getYVel() < STALL_THRESHOLD.get(data)
							&& data.getInputs().JUMP.isHeld()
					))
	);
	private static final Set<Identifier> NO_STALL_FROM = Set.of(ID, DUCK_STALL_ID);
	private static final TransitionInjectionDefinition.InjectionPredicate STALL_INJECTION_PREDICATE =
			(fromAction, fromCategory, existingTransitions) ->
					!NO_STALL_FROM.contains(fromAction)
					&& fromCategory == ActionCategory.AIRBORNE;
	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.AFTER,
						GroundPoundFlip.ID,
						STALL_INJECTION_PREDICATE,
						(nearbyTransition, castableHelper) -> STALL_TRANSITION
				),
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.AFTER,
						Fall.ID,
						STALL_INJECTION_PREDICATE,
						(nearbyTransition, castableHelper) -> DUCK_STALL_TRANSITION
				),
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.AFTER,
						Jump.ID,
						STALL_INJECTION_PREDICATE,
						(nearbyTransition, castableHelper) -> DUCK_STALL_TRANSITION
				)
		);
	}
}
