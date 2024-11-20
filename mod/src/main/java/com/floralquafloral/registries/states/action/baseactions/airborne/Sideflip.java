package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.util.MixedEasing.*;

public class Sideflip extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "sideflip");
	}
	@Override public @Nullable String getAnimationName() {
		return "sideflip";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return new CameraAnimationSet(
				new CameraAnimation(
						false, 1.0F,
						(progress, offsets) -> {
							offsets[0] = 180 * (1 - mixedEase(progress, SINE, SINE));
							offsets[2] = mixedEase(progress, SINE, CUBIC) * -360;
						}
				),
				null,
				null
		);
	}

	public static CharaStat SIDEFLIP_VEL = new CharaStat(1.065, StatCategory.JUMP_VELOCITY);
	public static CharaStat SIDEFLIP_BACKWARDS_SPEED = new CharaStat(-0.375,
			StatCategory.DRIFTING, StatCategory.BACKWARD, StatCategory.SPEED);
	public static CharaStat SIDEFLIP_THRESHOLD = new CharaStat(0.2,
			StatCategory.RUNNING, StatCategory.THRESHOLD);

	@Override
	public void airborneTravel(MarioTravelData data) {
		if(data.getYVel() < 0.1) airborneAccel(data);
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				AerialTransitions.GROUND_POUND,
				AerialTransitions.makeJumpCapTransition(this, 0.65)
		);
	}
}
