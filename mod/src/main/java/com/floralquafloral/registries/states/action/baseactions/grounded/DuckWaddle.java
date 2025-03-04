package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.airborne.Backflip;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.definitions.actions.StatCategory.*;

public class DuckWaddle extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_waddle");
	}
	@Override public @Nullable String getAnimationName() {
		return "duck_waddle";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return null;
	}

	public static final ActionTransitionDefinition UNDUCK = new ActionTransitionDefinition(
			"qua_mario:basic",
			(data) -> !data.getInputs().DUCK.isHeld(),
			null,
			(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.UNDUCK, seed)
	);
	public static final ActionTransitionDefinition DUCK_FALL = new ActionTransitionDefinition(
			"qua_mario:duck_fall",
			GroundedTransitions.FALL.EVALUATOR
	);
	public static final ActionTransitionDefinition DUCK_JUMP = new ActionTransitionDefinition(
			"qua_mario:duck_jump",
			GroundedTransitions.JUMP.EVALUATOR,
			data -> GroundedTransitions.performJump(data, GroundedTransitions.JUMP_VEL, null),
			(data, isSelf, seed) -> {
				data.voice(MarioClientSideData.VoiceLine.DUCK_JUMP, seed);
				data.playJumpSound(seed);
			}
	);
	public static final ActionTransitionDefinition BACKFLIP = new ActionTransitionDefinition(
			"qua_mario:backflip",
			data -> data.getForwardVel() < 0.0 && data.getInputs().getForwardInput() < -0.65 && data.getInputs().JUMP.isPressed(),
			data -> {
				GroundedTransitions.performJump(data, Backflip.BACKFLIP_VEL, null);
				data.getInputs().DUCK.isPressed();

				double backflipBackwardsVel = Backflip.BACKFLIP_BACKWARDS_SPEED.get(data);
				if(data.getForwardVel() > backflipBackwardsVel)
					data.setForwardStrafeVel(backflipBackwardsVel, 0.0);
			},
			(data, isSelf, seed) -> {
				data.playJumpSound(seed);
				data.voice(MarioClientSideData.VoiceLine.BACKFLIP, seed);
			}
	);

	public static final CharaStat WADDLE_ACCEL = new CharaStat(0.06, DUCKING, FORWARD, ACCELERATION);
	public static final CharaStat WADDLE_SPEED = new CharaStat(0.08, DUCKING, FORWARD, SPEED);

	public static final CharaStat WADDLE_BACKPEDAL_ACCEL = new CharaStat(0.0725, DUCKING, BACKWARD, ACCELERATION);
	public static final CharaStat WADDLE_BACKPEDAL_SPEED = new CharaStat(0.06, DUCKING, BACKWARD, SPEED);

	public static final CharaStat WADDLE_STRAFE_ACCEL = new CharaStat(0.06, DUCKING, STRAFE, ACCELERATION);
	public static final CharaStat WADDLE_STRAFE_SPEED = new CharaStat(0.06, DUCKING, STRAFE, SPEED);

	public static final CharaStat WADDLE_REDIRECTION = new CharaStat(0.0, DUCKING, REDIRECTION);

	@Override
	public void groundedTravel(MarioTravelData data) {
		boolean waddlingForward = data.getForwardVel() > 0;
		groundAccel(data,
				waddlingForward ? WADDLE_ACCEL : WADDLE_BACKPEDAL_ACCEL,
				waddlingForward ? WADDLE_SPEED : WADDLE_BACKPEDAL_SPEED,
				WADDLE_STRAFE_ACCEL, WADDLE_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(), WADDLE_REDIRECTION
		);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override public void serverTick(MarioAuthoritativeData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				UNDUCK
		);
	}

	@Override
	public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				BACKFLIP,
				DUCK_JUMP
		);
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:underwater_duck",
						GroundedTransitions.ENTER_WATER.EVALUATOR,
						GroundedTransitions.ENTER_WATER.EXECUTOR_TRAVELLERS,
						GroundedTransitions.ENTER_WATER.EXECUTOR_CLIENTS
				),
				DUCK_FALL
		);
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}
