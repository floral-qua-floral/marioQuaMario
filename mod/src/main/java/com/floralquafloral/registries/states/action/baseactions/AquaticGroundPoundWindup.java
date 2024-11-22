package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.AirborneActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.airborne.GroundPound;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.util.MixedEasing.*;

public class AquaticGroundPoundWindup extends GroundPoundWindup {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "aquatic_ground_pound_windup");
	}
	@Override public @Nullable String getAnimationName() {
		return "aquatic-ground-pound-windup";
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return new CameraAnimationSet(
				new CameraAnimation(
						false, 0.55F,
						(progress, offsets) -> offsets[1] = mixedEase(progress, SINE, CUBIC) * 360
				),
				null,
				null
		);
	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:aquatic_ground_pound",
						data -> AirborneActionDefinition.AerialTransitions.ENTER_WATER.EVALUATOR.shouldTransition(data)
								&& data.getTimers().actionTimer > 9,
						data -> {
							data.setYVel(GroundPound.GROUND_POUND_VEL.get(data));
							data.getInputs().JUMP.isPressed(); // Unbuffers jump
						},
						(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.DIVE, seed)
				),
				new ActionTransitionDefinition("qua_mario:ground_pound",
						data -> data.getTimers().actionTimer > 9,
						data -> {
							data.setYVel(GroundPound.GROUND_POUND_VEL.get(data));
							data.getInputs().JUMP.isPressed(); // Unbuffers jump
						}
				)
		);
	}
}
