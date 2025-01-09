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
				null,
				new BodyPartAnimation((data, arrangement, progress, loops) -> arrangement.setPos(0, 0, 0)),
				new BodyPartAnimation((data, arrangement, progress, loops) -> arrangement.addAngles(0, 0, 0)),

				new LimbAnimation(false, (data, arrangement, progress, loops) -> {
					float jumpProgress = Easing.clampedRangeToProgress(data.getYVel(), 0.87F, -0.85F);
					float scalingFactor = 0.3F;

					arrangement.setAngles(
							arrangement.pitch * -0.8F + Easing.QUINT_IN.ease(jumpProgress, -160, -30),
							arrangement.yaw * scalingFactor,
							arrangement.roll * scalingFactor
					);
				}),
				new LimbAnimation(true, (data, arrangement, progress, loops) -> arrangement.setAngles(15 + 1.2F * arrangement.pitch, arrangement.yaw, arrangement.roll)),

				new LimbAnimation(false, (data, arrangement, progress, loops) -> arrangement.setAngles(15, 0, 0)),
				new LimbAnimation(false, (data, arrangement, progress, loops) -> {
					float jumpProgress = Easing.clampedRangeToProgress(data.getYVel(), 0.87F, -0.85F);

					arrangement.setAngles(-10, 0, 0);
					arrangement.setPos(
							1.98F,
							Easing.EXPO_IN.ease(jumpProgress, 7, 12),
							Easing.QUART_IN.ease(jumpProgress, -2.5F, 0)
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
