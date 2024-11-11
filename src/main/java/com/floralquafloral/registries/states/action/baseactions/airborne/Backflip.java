package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;
import com.floralquafloral.util.AsymmetricalEasing;
import com.floralquafloral.util.Easings;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.util.AsymmetricalEasing.*;

public class Backflip extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "backflip");
	}
	@Override public @Nullable String getAnimationName() {
		return "backflip";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return new CameraAnimationSet(
				new CameraAnimation(
						false, 1.0F,
						(progress, offsets) -> offsets[1] = asymmetricEase(progress, easeInOutSine, easeInOutCubic) * -360
				),
				null,
				null
		);
	}

	public static CharaStat BACKFLIP_VEL = new CharaStat(1.065, StatCategory.JUMP_VELOCITY);
	public static CharaStat BACKFLIP_BACKWARDS_SPEED = new CharaStat(-0.16,
			StatCategory.DRIFTING, StatCategory.BACKWARD, StatCategory.SPEED);

	public static CharaStat REDUCED_FORWARD_ACCEL = AerialStats.FORWARD_DRIFT_ACCEL.variate(0.4);
	public static CharaStat REDUCED_FORWARD_SPEED = AerialStats.FORWARD_DRIFT_SPEED.variate(0.3);
	public static CharaStat REDUCED_BACKWARD_ACCEL = AerialStats.BACKWARD_DRIFT_ACCEL.variate(0.4);
	public static CharaStat REDUCED_BACKWARD_SPEED = AerialStats.BACKWARD_DRIFT_SPEED.variate(0.3);
	public static CharaStat REDUCED_STRAFE_ACCEL = AerialStats.STRAFE_DRIFT_ACCEL.variate(0.4);
	public static CharaStat REDUCED_STRAFE_SPEED = AerialStats.STRAFE_DRIFT_SPEED.variate(0.3);

	public static CharaStat BACKFLIP_REDIRECTION = new CharaStat(0, StatCategory.DRIFTING, StatCategory.REDIRECTION);

	@Override
	public void airborneTravel(MarioTravelData data) {
		if(data.getYVel() < 0.1) airborneAccel(data);
		else airborneAccel(data,
				REDUCED_FORWARD_ACCEL, REDUCED_FORWARD_SPEED,
				REDUCED_BACKWARD_ACCEL, REDUCED_BACKWARD_SPEED,
				REDUCED_STRAFE_ACCEL, REDUCED_STRAFE_SPEED,
				1, 1, BACKFLIP_REDIRECTION
		);
	}

	@Override public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				AerialTransitions.BASIC_LANDING
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				AerialTransitions.makeJumpCapTransition(this, 0.765)
		);
	}
}
