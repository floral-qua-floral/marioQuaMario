package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.MarioVars;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.fqf.mario_qua_mario_api.util.StatCategory.JUMPING_GRAVITY;
import static com.fqf.mario_qua_mario_api.util.StatCategory.JUMP_VELOCITY;

public class Jump extends Fall implements AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("jump");
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				(data, rightArmBusy, leftArmBusy, headRelativeYaw) -> {
					if(leftArmBusy && !rightArmBusy) return false;
					if(rightArmBusy && !leftArmBusy) return true;
					if(Math.abs(headRelativeYaw) > 0.55F) return headRelativeYaw > 0;
					return data.getMario().getRandom().nextBoolean();
				},
				new ProgressHandler(
						(data, ticksPassed) ->
								Easing.EXPO_IN_OUT.ease(Easing.clampedRangeToProgress(data.getYVel(), 0.87, -0.85))
				),
				null, null, null,

				new LimbAnimation(false, (data, arrangement, progress) -> {
					float scalingFactor = 0.3F;

					arrangement.setAngles(
							arrangement.pitch * -0.8F + Easing.QUINT_IN.ease(progress, -160, -30),
							arrangement.yaw * scalingFactor,
							arrangement.roll * scalingFactor
					);
				}),
				new LimbAnimation(false, (data, arrangement, progress) ->
						arrangement.setAngles(15 + 1.2F * arrangement.pitch, arrangement.yaw, arrangement.roll)),

				new LimbAnimation(false, (data, arrangement, progress) ->
						arrangement.addAngles(15, 0, 0)),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.setAngles(Easing.QUINT_IN.ease(progress, -30, -10), 0, 0);
					arrangement.addPos(
							0,
							Easing.EXPO_IN.ease(progress, -5, 0),
							Easing.QUART_IN.ease(progress, -2.5F, 0)
					);
				}),

				null
		);
	}

	public static final CharaStat JUMP_GRAVITY = new CharaStat(-0.095, JUMPING_GRAVITY);

	public static final CharaStat JUMP_VEL = new CharaStat(0.858, JUMP_VELOCITY);
	public static final CharaStat JUMP_ADDEND = new CharaStat(0.3, JUMP_VELOCITY);

	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
		Fall.drift(data, helper);
	}

	public static TransitionDefinition makeJumpTransition(GroundedActionDefinition.GroundedActionHelper helper) {
		return new TransitionDefinition(
				MarioQuaMarioContent.makeID("jump"),
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
