package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.BodyPartAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.EntireBodyAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.airborne.Sideflip;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Skid implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("skid");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					arrangement.y -= 4;
					arrangement.setAngles(
							0,
							42.5F,
							17.5F
					);
				},
				(posture, data, animationTime, helper) -> {
					posture.HEAD.roll -= 15;

					posture.RIGHT_ARM.addAngles(-32, -35, 80);
					posture.LEFT_ARM.addAngles(-45, 0, -30);
					posture.RIGHT_LEG.addAngles(-57.5F, 45F, -20);
					posture.LEFT_LEG.addPos(0, -4.1F, -3.9F);
					posture.LEFT_LEG.addAngles(5, 15, 0);

					if(posture.TAIL != null)
						posture.TAIL.setAngles(5, 42, -17.5F);
				}
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
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

	@Override public @Nullable BappingRule getBappingRule() {
		return new BappingRule(0, 0, 3, new CfaStat(0, THRESHOLD));
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	public static final CfaStat SKID_THRESHOLD = new CfaStat(0.285, RUNNING, THRESHOLD);

	public static final CfaStat SKID_DRAG = new CfaStat(0.185, RUNNING, DRAG);
	public static final CfaStat SKID_DRAG_MIN = new CfaStat(0.045, RUNNING, DRAG);
	public static final CfaStat SKID_REDIRECTION = new CfaStat(4.5, RUNNING, REDIRECTION);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
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
						SubWalk.ID,
						data -> data.getHorizVelSquared() == 0 || data.getInputs().getForwardInput() >= 0,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						Sideflip.ID,
						data -> data.getForwardVel() < Sideflip.SIDEFLIP_THRESHOLD.get(data) && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							helper.performJump(data, Sideflip.SIDEFLIP_VEL, null);
							data.setForwardStrafeVel(Sideflip.SIDEFLIP_BACKWARDS_SPEED.get(data), 0);
							data.forceBodyAlignment(true);
						},
						(data, isSelf, seed) -> {
							data.forceBodyAlignment(true);
							data.playJumpSound(seed);
							data.voice(Voicelines.SIDEFLIP, seed);
						}
				),
				Jump.makeJumpTransition(helper)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL,
				UnderwaterWalk.SUBMERGE,
				BonkAir.BONK.variate(
						BonkGroundBackward.ID,
						null,
						null,
						data -> {
							data.setVelocity(data.getRecordedCollisions().getHorizontallyReflectedVelocity().multiply(1.25));
						},
						null
				)
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
