package com.fqf.mario_qua_mario_content.actions.power;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario_content.actions.airborne.Jump;
import com.fqf.mario_qua_mario_content.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario_content.powerups.Raccoon;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import com.fqf.mario_qua_mario_content.util.Powers;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class TailStall extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("tail_stall");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.x -= factor;
			arrangement.roll += 100 * factor + MathHelper.sin(progress) * 5;
			arrangement.pitch += factor * MathHelper.cos(progress) * 11;
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += factor * MathHelper.cos(progress) * 70F;
		});
	}
	public static LimbAnimation makeTailAnimation(boolean useProgress) {
		return new LimbAnimation(
				false, (data, arrangement, progress) -> {
					float value;
					if(useProgress) value = progress;
					else value = data.getVars(ActionTimerVars.class).actionTimer * 1.1F;
					arrangement.setAngles(
							MathHelper.sin(value * 1.2F) * 38.3F,
							MathHelper.sin(value * 0.6F) * 52,
							0
					);
				}
		);
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> ticksPassed * 1.1F),
				null,
				null,
				null,
				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
				makeTailAnimation(true)
		);
	}

	public static final CharaStat FALL_ACCEL = new CharaStat(-0.013775, NORMAL_GRAVITY, POWER_UP);
	public static final CharaStat FALL_SPEED = new CharaStat(-0.445, TERMINAL_VELOCITY, POWER_UP);

	public static void tailWaggleTick(IMarioClientData data) {
		if(data.getVars(ActionTimerVars.class).actionTimer++ % 4 == 0)
			data.playSound(MarioContentSFX.TAIL_FLY, 1F, 0.2F, data.getMario().getRandom().nextLong());
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		tailWaggleTick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		drift(data, helper);
		if(data.getYVel() > FALL_SPEED.get(data)) data.getMario().fallDistance = 0;
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

	public static final CharaStat STALL_THRESHOLD = new CharaStat(-0.31, THRESHOLD, POWER_UP);
	private static final TransitionDefinition STALL_TRANSITION = new TransitionDefinition(
			TailStall.ID,
			data ->
					data.hasPower(Powers.TAIL_STALL)
					&& !data.getMario().isInSneakingPose()
					&& (data.isServer() || (
							data.getYVel() < STALL_THRESHOLD.get(data)
							&& data.getInputs().JUMP.isHeld()
					)),
			EvaluatorEnvironment.CLIENT_CHECKED,
			data -> {
				Raccoon.RaccoonVars vars = data.getVars(Raccoon.RaccoonVars.class);

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
	protected static final Identifier DUCK_STALL_ID = MarioQuaMarioContent.makeID("tail_stall_duck");
	private static final TransitionDefinition DUCK_STALL_TRANSITION = STALL_TRANSITION.variate(
			DUCK_STALL_ID,
			data ->
					data.hasPower(Powers.TAIL_STALL)
					&& data.getMario().isInSneakingPose()
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
