package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.Backflip;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterDuck;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class DuckWaddle implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("duck_waddle");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	public static final Identifier ANIMATION_ID = MarioQuaMario.makeID("grounded_ducking");
	public static final Identifier AIRBORNE_ANIMATION_ID = MarioQuaMario.makeID("airborne_ducking");
	private static float getDuckProgress(float animationTime) {
		// We animate ducking tick by tick and rely on interpolation, rather than trying to model this using math. #lazy
		return switch(MathHelper.floor(animationTime)) {
			case 0 -> 0.6F;
			case 1 -> 1.21F;
			case 2 -> 1.45F;
			default -> 1;
		};
	}
	public static AnimationDefinition makeAnimation(boolean isGrounded, boolean isWaddle) {
		Identifier animationID = isGrounded ? ANIMATION_ID : AIRBORNE_ANIMATION_ID;
		return AnimationDefinition.of(
				animationID,
				isWaddle ? AnimationFlag.NO_SWING_ARMS : AnimationFlag.NO_SWING_LIMBS,
				(data, prevID) -> // If previously in a grounded duck, then do not replay the squishy crouch anim. Otherwise, do.
						ANIMATION_ID.equals(prevID) || animationID.equals(prevID)
								? EnumSet.of(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS)
								: AnimationFlag.Execution.NONE,
				isGrounded ? null : (arrangement, data, animationTime, helper) -> // Don't need a model arranger if grounded!
						arrangement.pitch = (Easing.clampedRangeToProgress(data.getYVel(), -0.0, 0.4) * 2 - 1) * 15F,
				(posture, data, animationTime, helper) -> {
					float progress = getDuckProgress(animationTime);

					posture.HEAD.addPos(0, progress * 14.2F, progress * -3.5F);
					if(progress > 1) posture.HEAD.z -= 2 * (progress - 1);

					posture.TORSO.pitch += progress * 41.65F;
					posture.TORSO.addPos(0, progress * 11.9F, progress * -3F);

					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
						arrangement.addPos(0, progress * 14.2F, -1);
						arrangement.pitch = Easing.LINEAR.ease(
								Math.min(progress, 1),
								arrangement.pitch,
								-162.92F + 0.26F * Math.min(data.getPlayer().getPitch(), 0) + -1.15F * arrangement.pitch
						);
						if(Math.abs(arrangement.roll) < 10) arrangement.roll = 0;
					});

					helper.symmetricallyAnimate(posture, posture.RIGHT_LEG, arrangement -> {
						arrangement.addPos(0, progress * 2.2F, progress * -4.4F);
						arrangement.addAngles(progress * 25, 0, 0);
					});

					if(posture.TAIL != null) {
						posture.TAIL.pitch = -posture.TORSO.pitch + (isGrounded ? -9 : posture.TAIL.pitch);
					}
				}
		);
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return makeAnimation(true, true);
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

	public static final CfaStat WADDLE_ACCEL = new CfaStat(0.06, DUCKING, FORWARD, ACCELERATION);
	public static final CfaStat WADDLE_SPEED = new CfaStat(0.08, DUCKING, FORWARD, SPEED);

	public static final CfaStat WADDLE_BACKPEDAL_ACCEL = new CfaStat(0.0725, DUCKING, BACKWARD, ACCELERATION);
	public static final CfaStat WADDLE_BACKPEDAL_SPEED = new CfaStat(0.06, DUCKING, BACKWARD, SPEED);

	public static final CfaStat WADDLE_STRAFE_ACCEL = new CfaStat(0.06, DUCKING, STRAFE, ACCELERATION);
	public static final CfaStat WADDLE_STRAFE_SPEED = new CfaStat(0.06, DUCKING, STRAFE, SPEED);

	public static final CfaStat WADDLE_REDIRECTION = new CfaStat(0.0, DUCKING, REDIRECTION);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		boolean waddlingForward = data.getInputs().getForwardInput() > 0;
		helper.groundAccel(data,
				waddlingForward ? WADDLE_ACCEL : WADDLE_BACKPEDAL_ACCEL,
				waddlingForward ? WADDLE_SPEED : WADDLE_BACKPEDAL_SPEED,
				WADDLE_STRAFE_ACCEL, WADDLE_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				WADDLE_REDIRECTION
		);
	}

	public static final TransitionDefinition DUCK = new TransitionDefinition(
			DuckWaddle.ID,
			data -> data.getInputs().DUCK.isHeld(),
			EvaluatorEnvironment.CLIENT_ONLY,
			null,
			(data, isSelf, seed) -> {
				data.playSound(MarioSFX.DUCK, seed);
				data.voice(Voicelines.DUCK, seed);
			}
	);

	public static final TransitionDefinition UNDUCK = new TransitionDefinition(
			SubWalk.ID,
			data -> !data.getInputs().DUCK.isHeld(),
			EvaluatorEnvironment.CLIENT_ONLY,
			null,
			(data, isSelf, seed) -> data.playSound(MarioSFX.UNDUCK, seed)
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				UNDUCK
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				Backflip.makeBackflipTransition(helper),
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
		return Set.of();
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
