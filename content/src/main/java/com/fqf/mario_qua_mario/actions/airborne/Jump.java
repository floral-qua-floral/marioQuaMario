package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

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
				(data, ticksPassed) -> Easing.EXPO_IN_OUT.ease(Easing.clampedRangeToProgress(data.getYVel(), 0.87F, -0.85F)),
				null,
				null,
				null,

				new LimbAnimation(false, (data, arrangement, progress) -> {
					float scalingFactor = 0.3F;

					arrangement.setAngles(
							arrangement.pitch * -0.8F + Easing.QUINT_IN.ease(progress, -160, -30),
							arrangement.yaw * scalingFactor,
							arrangement.roll * scalingFactor
					);
//					arrangement.addPos(-10, 0, 0);
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
	public static final CharaStat JUMP_ADDEND = new CharaStat(0.2, JUMP_VELOCITY);

	public static TransitionDefinition makeJumpTransition(GroundedActionDefinition.GroundedActionHelper helper) {
		return new TransitionDefinition(
				MarioQuaMarioContent.makeID("jump"),
				data -> data.getInputs().JUMP.isPressed(),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> helper.performJump(data, JUMP_VEL, JUMP_ADDEND),
				(data, isSelf, seed) -> data.playJumpSound(seed)
		);
	}

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
	}
}
