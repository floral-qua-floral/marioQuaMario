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

public class Jump extends AirborneActionDefinition {
	public static final CharaStat JUMP_VEL = new CharaStat(0.858, StatCategory.JUMP_VELOCITY);
	public static final CharaStat JUMP_ADDEND = new CharaStat(0.117, StatCategory.JUMP_VELOCITY);

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "jump");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}

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
		return AerialStats.GRAVITY;
	}
	@Override protected @Nullable CharaStat getJumpGravity() {
		return AerialStats.JUMP_GRAVITY;
	}
	@Override protected @NotNull CharaStat getTerminalVelocity() {
		return AerialStats.TERMINAL_VELOCITY;
	}
	@Override protected @Nullable CharaStat getJumpCap() {
		return AerialStats.JUMP_CAP;
	}

	@Override public void aerialSelfTick(MarioClientData data) {
		airborneAccel(data);
	}

	@Override public void otherClientsTick(MarioPlayerData data) {

	}

	@Override public void serverTick(MarioPlayerData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
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
