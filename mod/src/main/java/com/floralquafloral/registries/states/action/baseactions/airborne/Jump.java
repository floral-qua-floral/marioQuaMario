package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.AirborneActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Jump extends AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "jump");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return BumpingRule.JUMPING;
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
		return AerialStats.JUMP_GRAVITY;
	}
	@Override protected @NotNull CharaStat getTerminalVelocity() {
		return AerialStats.TERMINAL_VELOCITY;
	}

	@Override public void airborneTravel(MarioTravelData data) {
		airborneAccel(data);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(

		);
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				AerialTransitions.GROUND_POUND,
				AerialTransitions.makeJumpCapTransition(this, 0.39)
		);
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AerialTransitions.ENTER_WATER,
				AerialTransitions.DOUBLE_JUMPABLE_LANDING
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of(
				Kick.KICK
		);
	}
}
