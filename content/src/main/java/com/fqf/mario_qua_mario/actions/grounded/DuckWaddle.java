package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class DuckWaddle implements GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("duck_waddle");
	}

	private static final LimbAnimation ARM = new LimbAnimation(false, (data, arrangement, progress) -> {
		arrangement.addPos(0, progress * 9, -1);
		arrangement.pitch = Easing.LINEAR.ease(Math.min(progress, 1), arrangement.pitch, -140 + 0.26F * Math.min(data.getMario().getPitch(), 0) + -1.15F * arrangement.pitch);
		if(Math.abs(arrangement.roll) < 10) arrangement.roll = 0;
	});
	private static LimbAnimation makeLegAnimation(boolean walking) {
		return new LimbAnimation(walking, (data, arrangement, progress) -> {
			arrangement.addPos(0, 0, progress * -8.4F);
			arrangement.addAngles(progress * 25, 0, 0);
		});
	}
	private static final Identifier DUCK_ANIM_ID = MarioQuaMarioContent.makeID("duck");
	private static final Identifier DUCK_AIR_ANIM_ID = MarioQuaMarioContent.makeID("duck_air");
	public static PlayermodelAnimation makeDuckAnimation(boolean walking, boolean airborne) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler(
						airborne ? DUCK_AIR_ANIM_ID : DUCK_ANIM_ID,
						(data, prevAnimationID) ->
								!prevAnimationID.equals(DUCK_ANIM_ID) && !(airborne && prevAnimationID.equals(DUCK_AIR_ANIM_ID)),
						(data, ticksPassed) -> switch(ticksPassed) {
							case 0 -> 0.6F;
							case 1 -> 1.34F;
							case 2 -> 1.48F;
							default -> 1;
						}),
				null,

				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.addPos(0, progress * 8, progress * -3.5F);
					if(progress > 1) arrangement.z -= 2 * (progress - 1);
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.addAngles(progress * 13, 0, 0);
					arrangement.addPos(0, progress * 6.7F, progress * -3F);
				}),

				ARM, ARM, makeLegAnimation(walking), makeLegAnimation(walking), null
		);
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return makeDuckAnimation(true, false);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
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

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static final CharaStat WADDLE_ACCEL = new CharaStat(0.06, DUCKING, FORWARD, ACCELERATION);
	public static final CharaStat WADDLE_SPEED = new CharaStat(0.08, DUCKING, FORWARD, SPEED);

	public static final CharaStat WADDLE_BACKPEDAL_ACCEL = new CharaStat(0.0725, DUCKING, BACKWARD, ACCELERATION);
	public static final CharaStat WADDLE_BACKPEDAL_SPEED = new CharaStat(0.06, DUCKING, BACKWARD, SPEED);

	public static final CharaStat WADDLE_STRAFE_ACCEL = new CharaStat(0.06, DUCKING, STRAFE, ACCELERATION);
	public static final CharaStat WADDLE_STRAFE_SPEED = new CharaStat(0.06, DUCKING, STRAFE, SPEED);

	public static final CharaStat WADDLE_REDIRECTION = new CharaStat(0.0, DUCKING, REDIRECTION);

	@Override public @Nullable Object setupCustomMarioVars() {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
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
			MarioQuaMarioContent.makeID("duck_waddle"),
			data -> data.getInputs().DUCK.isHeld(),
			EvaluatorEnvironment.CLIENT_ONLY,
			null,
			(data, isSelf, seed) -> {
				data.playSound(MarioContentSFX.DUCK, seed);
				data.voice("duck", seed);
			}
	);

	public static final TransitionDefinition UNDUCK = new TransitionDefinition(
			MarioQuaMarioContent.makeID("sub_walk"),
			data -> !data.getInputs().DUCK.isHeld(),
			EvaluatorEnvironment.CLIENT_ONLY,
			null,
			(data, isSelf, seed) -> data.playSound(MarioContentSFX.UNDUCK, seed)
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				UNDUCK,
				DuckJump.makeDuckJumpTransition(helper)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckFall.DUCK_FALL
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of();
	}
}
