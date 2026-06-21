package com.fqf.mario_qua_mario.actions.form;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario.actions.grounded.DuckSlide;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.forms.Raccoon;
import com.fqf.mario_qua_mario.util.Powers;
import com.fqf.mario_qua_mario.util.TailSpinActionTimerVars;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TailSpinGround implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("tail_spin_grounded");

	private static final float TICKS_PER_REVOLUTION = 6;
	public static AnimationDefinition makeAnimation(boolean isGrounded) {
		return AnimationDefinition.layerModelArranger(
				AnimationDefinition.layerPostureMutator(
						DuckWaddle.makeAnimation(isGrounded, false),
						((posture, data, animationTime, helper) -> {
							if(posture.TAIL != null)
								posture.TAIL.pitch = -MathHelper.clamp(data.getPlayer().getPitch() - 10, -60, 10) - posture.TORSO.pitch;
						})
				),
				(arrangement, data, animationTime, helper) -> {
					arrangement.yaw = data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer / TICKS_PER_REVOLUTION * 360;
				}
		);
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return makeAnimation(true);
	}

	private static CameraAnimation makeTailSpinCameraAnimation(float factor, Easing easing) {
		return new CameraAnimation(
				new CameraProgressHandler(2, (data, ticksPassed) -> {
					if(data.retrieveStateData(TailSpinActionTimerVars.class) == null) return 4;
					return (ticksPassed / (TICKS_PER_REVOLUTION - 0.5F) * factor) % 1;
				}),
				(data, arrangement, progress) -> arrangement.yaw += easing.ease(progress % 1) * -360
		);
	}
	public static final CameraAnimationSet CAMERA_ANIMATIONS = new CameraAnimationSet(
		MarioQuaMario.CONFIG::getTailSpinCameraAnim,
		makeTailSpinCameraAnimation(1, Easing.QUAD_IN_OUT),
		makeTailSpinCameraAnimation(0.5F, Easing.SINE_IN_OUT),
		null
	);

	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return CAMERA_ANIMATIONS;
	}
	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.SLIP;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}

	public static void attemptTailStrike(CfaAuthoritativeData data) {
		if(data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer % 3 == 0) {
			ServerPlayerEntity mario = data.getPlayer();
			ImmutableList.Builder<Entity> strikeTargets = ImmutableList.builder();

			strikeTargets.addAll(mario.getServerWorld().getOtherEntities(
					mario,
					mario.getBoundingBox().expand(1, 0.5, 1),
					Entity::canHit
			));
			strikeTargets.addAll(mario.getServerWorld().getOtherEntities(
					mario,
					mario.getBoundingBox().expand(3, 1, 3),
					Raccoon::canBeReflected
			));

			DamageSource source = mario.getDamageSources().playerAttack(mario);
			for(Entity strikeTarget : strikeTargets.build()) {
				if(!Raccoon.tryReflect(strikeTarget, mario, false)) {
					strikeTarget.damage(source, Raccoon.TAIL_STRIKE_DAMAGE);
					mario.onAttacking(strikeTarget);
				}
			}
		}
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new TailSpinActionTimerVars(data);
	}
	public static void commonTick(CfaData data) {
		data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer++;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		commonTick(data);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		commonTick(data);
		attemptTailStrike(data);
	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		helper.applyDrag(data, CfaStat.ZERO, CfaStat.ZERO,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(), DuckSlide.SLIDE_REDIRECTION);
	}

	public static boolean doneSpinning(CfaData data) {
		return data.retrieveStateData(TailSpinActionTimerVars.class).actionTimer >= 2 * TICKS_PER_REVOLUTION;
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				DuckWaddle.UNDUCK,
				new ActionTransitionDetails(
						DuckWaddle.ID,
						data -> !data.hasPower(Powers.TAIL_ATTACK)
								|| doneSpinning(data),
						EvaluatorEnvironment.COMMON
				)
		);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
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
}
