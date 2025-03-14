package com.fqf.mario_qua_mario.actions.power;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario.actions.grounded.DuckSlide;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import com.fqf.mario_qua_mario.util.Powers;
import com.fqf.mario_qua_mario.util.TailSpinActionTimerVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class TailSpinGround implements GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("tail_spin_grounded");
	}

	private static final float TICKS_PER_REVOLUTION = 6;
	public static final PlayermodelAnimation ANIMATION = DuckWaddle.makeDuckAnimation(false, true).variate(
			null,
			null,
			new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
				arrangement.yaw = Easing.LINEAR.ease((data.getVars(TailSpinActionTimerVars.class).actionTimer / TICKS_PER_REVOLUTION) % 1) * 360;
			}),
			null,
			null,
			null,
			null,
			null,
			null,
			new LimbAnimation(false, (data, arrangement, progress) -> {
				arrangement.pitch = MathHelper.clamp(data.getMario().getPitch() - 10, -80, 10);
			})
	);

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return ANIMATION;
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.SLIP;
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

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new TailSpinActionTimerVars(data);
	}
	public static void commonTick(IMarioData data) {
		data.getVars(TailSpinActionTimerVars.class).actionTimer++;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		commonTick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {
		commonTick(data);
	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		helper.applyDrag(data, CharaStat.ZERO, CharaStat.ZERO,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(), DuckSlide.SLIDE_REDIRECTION);
	}

	public static boolean doneSpinning(IMarioData data) {
		return data.getVars(TailSpinActionTimerVars.class).actionTimer >= 2 * TICKS_PER_REVOLUTION;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("duck_waddle"),
						data -> !data.hasPower(Powers.TAIL_ATTACK)
								|| doneSpinning(data),
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckWaddle.UNDUCK,
				DuckJump.makeDuckJumpTransition(helper).variate(
						MarioQuaMarioContent.makeID("tail_spin_jump"),
						null, null,
						data -> {
							helper.performJump(data, TailSpinJump.JUMP_VEL, null);
						},
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
						}
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckFall.DUCK_FALL.variate(MarioQuaMarioContent.makeID("tail_spin_fall"), null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
