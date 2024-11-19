package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideDataImplementation;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.states.action.ActionDefinition;
import com.floralquafloral.registries.states.action.AirborneActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.airborne.Backflip;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GroundPound implements ActionDefinition {
	public static final CharaStat GROUND_POUND_VEL = new CharaStat(-1.5, StatCategory.TERMINAL_VELOCITY);

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "ground_pound");
	}
	@Override public @Nullable String getAnimationName() {
		return "ground-pound";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return BumpingRule.GROUND_POUND;
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of("qua_mario", "ground_pound");
	}

	@Override public void travelHook(MarioTravelData data) {
		data.setYVel(GROUND_POUND_VEL.get(data));
		AirborneActionDefinition.airborneAccel(data,
				AirborneActionDefinition.AerialStats.FORWARD_DRIFT_ACCEL, Backflip.REDUCED_FORWARD_SPEED,
				AirborneActionDefinition.AerialStats.BACKWARD_DRIFT_ACCEL, Backflip.REDUCED_BACKWARD_SPEED,
				AirborneActionDefinition.AerialStats.STRAFE_DRIFT_ACCEL, Backflip.REDUCED_STRAFE_SPEED,
				1, 1, Backflip.BACKFLIP_REDIRECTION
		);
	}

	@Override public void clientTick(MarioClientSideDataImplementation data, boolean isSelf) {

	}

	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of(
				new ActionTransitionDefinition(
						"qua_mario:ground_pound_landing",
						AirborneActionDefinition.AerialTransitions.BASIC_LANDING.EVALUATOR,
						data -> data.setForwardStrafeVel(0, 0),
						(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.GROUND_POUND, seed)
				)
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}
}
