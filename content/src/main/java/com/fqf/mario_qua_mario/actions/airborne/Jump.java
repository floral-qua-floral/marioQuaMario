package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.BappingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

import static com.fqf.charaformact_api.util.StatCategory.JUMPING_GRAVITY;
import static com.fqf.charaformact_api.util.StatCategory.JUMP_VELOCITY;

public class Jump extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("jump");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	public static float getAnimationProgress(CfaAnimatingData data) {
		return Easing.EXPO_IN_OUT.ease(Easing.clampedRangeToProgress(data.getYVel(), 0.87, -0.85));
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(data, prevAnimationID) -> {
					switch(data.getCurrentHandPreference()) {
						case PREFER_RIGHT -> {
							return EnumSet.noneOf(AnimationFlag.Execution.class);
						}
						case PREFER_LEFT -> {
							return EnumSet.of(AnimationFlag.Execution.MIRROR);
						}
						default -> {
							if(Math.abs(data.getRelativeHeadYawDegrees()) > 55) {
								if(data.getRelativeHeadYaw() > 0) return EnumSet.of(AnimationFlag.Execution.MIRROR);
								else return EnumSet.noneOf(AnimationFlag.Execution.class);
							}
							if(data.getPlayer().getRandom().nextBoolean()) return EnumSet.of(AnimationFlag.Execution.MIRROR);
							else return EnumSet.noneOf(AnimationFlag.Execution.class);
						}
					}
				},
				(posture, data, animationTime, helper) -> {
					float progress = getAnimationProgress(data);

					float scalingFactor = 0.3F;
					posture.RIGHT_ARM.setAngles(
							posture.RIGHT_ARM.pitch * -0.8F + Easing.QUINT_IN.ease(progress, -160, -30),
							posture.RIGHT_ARM.yaw * scalingFactor,
							posture.RIGHT_ARM.roll * scalingFactor
					);

					posture.LEFT_ARM.pitch = 15 + 1.2F * posture.LEFT_ARM.pitch;

					posture.RIGHT_LEG.pitch += 15;

					posture.LEFT_LEG.setAngles(Easing.QUINT_IN.ease(progress, -30, -10), 0, 0);
					posture.LEFT_LEG.addPos(
							0,
							Easing.EXPO_IN.ease(progress, -5, 0),
							Easing.QUART_IN.ease(progress, -2.5F, 0)
					);
				}
		);
	}
	@Override public @Nullable BappingRule getBappingRule() {
		return BappingRule.JUMPING;
	}

	public static final CfaStat JUMP_GRAVITY = new CfaStat(-0.095, JUMPING_GRAVITY);

	public static final CfaStat JUMP_VEL = new CfaStat(0.858, JUMP_VELOCITY);
	public static final CfaStat JUMP_ADDEND = new CfaStat(0.3, JUMP_VELOCITY);

	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
		Fall.drift(data, helper);
	}

	public static TransitionDefinition makeJumpTransition(GroundedActionDefinition.GroundedActionHelper helper) {
		return new TransitionDefinition(
				Jump.ID,
				data -> data.getInputs().JUMP.isPressed(),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> helper.performJump(data, JUMP_VEL, JUMP_ADDEND),
				(data, isSelf, seed) -> data.playJumpSound(seed)
		);
	}

	public static final TransitionDefinition DOUBLE_JUMPABLE_LANDING = Fall.LANDING.variate(
			null, null, null,
			data -> MarioVars.get(data).canDoubleJumpTicks = 3,
			null
	);

	protected double getJumpCapThreshold() {
		return 0.39;
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				GroundPoundFlip.GROUND_POUND,
				helper.makeJumpCapTransition(this, this.getJumpCapThreshold())
		);
	}
	@Override protected TransitionDefinition getLandingTransition() {
		return DOUBLE_JUMPABLE_LANDING;
	}
}
