package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.states.action.AirborneActionDefinition;
import com.floralquafloral.stats.CharaStat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Fall extends AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "fall");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override
	public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of("qua_mario", "stomp");
	}

	@Override protected @NotNull CharaStat getGravity() {
		return AerialStats.GRAVITY;
	}
	@Override protected @Nullable CharaStat getJumpGravity() {
		return null;
	}
	@Override protected @NotNull CharaStat getTerminalVelocity() {
		return AerialStats.TERMINAL_VELOCITY;
	}

	@Override public void airborneTravel(MarioTravelData data) {
		airborneAccel(data);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override public void serverTick(MarioServerData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				AerialTransitions.BASIC_LANDING
		);
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}
}
