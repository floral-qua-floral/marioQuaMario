package com.fqf.mario_qua_mario_content.actions.power;

import com.fqf.charapoweract_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.*;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.EntireBodyAnimation;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charapoweract_api.cpadata.*;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract_api.cpadata.ICPAData;
import com.fqf.charapoweract_api.util.CharaStat;
import com.fqf.charapoweract_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario_content.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario_content.actions.grounded.DuckSlide;
import com.fqf.mario_qua_mario_content.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario_content.powerups.Raccoon;
import com.fqf.mario_qua_mario_content.util.Powers;
import com.fqf.mario_qua_mario_content.util.TailSpinActionTimerVars;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class TailSpinGround implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("tail_spin_grounded");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	private static final float TICKS_PER_REVOLUTION = 6;
	public static final PlayermodelAnimation ANIMATION = DuckWaddle.makeDuckAnimation(false, true).variate(
			null,
			null,
			new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
				arrangement.yaw = Easing.LINEAR.ease((data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer / TICKS_PER_REVOLUTION) % 1) * 360;
			}),
			null,
			null,
			null,
			null,
			null,
			null,
			new LimbAnimation(false, (data, arrangement, progress) -> {
				arrangement.pitch = MathHelper.clamp(data.getPlayer().getPitch() - 10, -80, 10);
			})
	);
	private static CameraAnimation makeTailSpinCameraAnimation(float factor, Easing easing) {
		return new CameraAnimation(
				new CameraProgressHandler(2, (data, ticksPassed) ->
				{
					if(data.retrieveStateData(TailSpinActionTimerVars.class) == null) return 4;
					return (ticksPassed / (TICKS_PER_REVOLUTION - 0.5F) * factor) % 1;
				}),
				(data, arrangement, progress) ->
						arrangement.yaw += easing.ease(progress % 1) * -360
		);
	}
	public static final CameraAnimationSet CAMERA_ANIMATIONS = new CameraAnimationSet(
		MarioQuaMarioContent.CONFIG::getTailSpinCameraAnim,
		makeTailSpinCameraAnimation(1, Easing.QUAD_IN_OUT),
		makeTailSpinCameraAnimation(0.5F, Easing.SINE_IN_OUT),
		null
	);

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return ANIMATION;
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return CAMERA_ANIMATIONS;
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

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	public static void attemptTailStrike(ICPAAuthoritativeData data) {
		if(data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer % 3 == 0) {
			ServerPlayerEntity mario = data.getPlayer();
			List<Entity> strikeTargets = mario.getServerWorld().getOtherEntities(
					mario, mario.getBoundingBox().expand(1, 0.5, 1));

			strikeTargets.removeIf(entity -> !entity.canHit());

			DamageSource source = mario.getDamageSources().playerAttack(mario);
			for(Entity strikeTarget : strikeTargets) {
				strikeTarget.damage(source, Raccoon.TAIL_STRIKE_DAMAGE);
			}
		}
	}

	@Override public @Nullable Object provideStateData(ICPAData data) {
		return new TailSpinActionTimerVars(data);
	}
	public static void commonTick(ICPAData data) {
		data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer++;
	}
	@Override public void clientTick(ICPAClientData data, boolean isSelf) {
		commonTick(data);
	}
	@Override public void serverTick(ICPAAuthoritativeData data) {
		commonTick(data);
		attemptTailStrike(data);
	}
	@Override public void travelHook(ICPATravelData data, GroundedActionHelper helper) {
		helper.applyDrag(data, CharaStat.ZERO, CharaStat.ZERO,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(), DuckSlide.SLIDE_REDIRECTION);
	}

	public static boolean doneSpinning(ICPAData data) {
		return data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer >= 2 * TICKS_PER_REVOLUTION;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckWaddle.UNDUCK,
				new TransitionDefinition(
						DuckWaddle.ID,
						data -> !data.hasPower(Powers.TAIL_ATTACK)
								|| doneSpinning(data),
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckJump.makeDuckJumpTransition(helper).variate(
						TailSpinJump.ID,
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
				DuckFall.DUCK_FALL.variate(TailSpinFall.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
