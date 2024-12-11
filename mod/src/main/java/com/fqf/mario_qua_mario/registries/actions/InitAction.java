package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.actions.ActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class InitAction implements ActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMario.makeID("init");
	}

	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
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

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data) {

	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of(
				new TransitionDefinition(
						MarioQuaMario.makeID("init"),
						data -> false
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions() {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions() {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}
