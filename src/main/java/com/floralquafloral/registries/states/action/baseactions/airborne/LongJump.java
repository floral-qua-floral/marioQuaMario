package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.AirborneActionDefinition;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LongJump extends AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "long_jump");
	}
	@Override public @Nullable String getAnimationName() {
		return "long-jump";
	}

	public static final CharaStat GRAVITY = new CharaStat(-0.0735, StatCategory.NORMAL_GRAVITY);

	public static final CharaStat LONG_JUMP_VEL = new CharaStat(0.67, StatCategory.JUMP_VELOCITY);
	public static final CharaStat LONG_JUMP_THRESHOLD = new CharaStat(0.3, StatCategory.THRESHOLD);

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of("qua_mario", "stomp");
	}

	@Override protected @NotNull CharaStat getGravity() {
		return GRAVITY;
	}
	@Override protected @Nullable CharaStat getJumpGravity() {
		return GRAVITY;
	}
	@Override protected @NotNull CharaStat getTerminalVelocity() {
		return AerialStats.TERMINAL_VELOCITY;
	}
	@Override protected @Nullable CharaStat getJumpCap() {
		return null;
	}

	@Override public void airborneTravel(MarioClientData data) {
		airborneAccel(data);
	}

	@Override public void clientTick(MarioPlayerData data, boolean isSelf) {

	}

	@Override public void serverTick(MarioPlayerData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				AerialTransitions.DUCKING_LANDING,
				AerialTransitions.DOUBLE_JUMPABLE_LANDING
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