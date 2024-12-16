package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class SubWalk implements GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("sub_walk");
	}

	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
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
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {

	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(

		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(

		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}
