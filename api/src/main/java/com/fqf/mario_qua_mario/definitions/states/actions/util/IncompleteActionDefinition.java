package com.fqf.mario_qua_mario.definitions.states.actions.util;

import com.fqf.mario_qua_mario.definitions.states.AttackInterceptingStateDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IncompleteActionDefinition extends AttackInterceptingStateDefinition {
	@Nullable String getAnimationName();
	@Nullable CameraAnimationSet getCameraAnimations();
	@NotNull SlidingStatus getSlidingStatus();

	@NotNull SneakingRule getSneakingRule();
	@NotNull SprintingRule getSprintingRule();
	
	@Nullable BumpType getBumpType();
	@Nullable Identifier getStompTypeID();

	@NotNull Set<TransitionInjectionDefinition> getTransitionInjections();
}
