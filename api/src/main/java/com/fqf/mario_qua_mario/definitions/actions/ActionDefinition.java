package com.fqf.mario_qua_mario.definitions.actions;

import com.fqf.mario_qua_mario.definitions.AttackInterceptingStateDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.*;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface ActionDefinition extends AttackInterceptingStateDefinition {
	@Nullable String getAnimationName();
	@Nullable CameraAnimationSet getCameraAnimations();
	@NotNull SlidingStatus getSlidingStatus();

	@NotNull SneakingRule getSneakingRule();
	@NotNull SprintingRule getSprintingRule();
	
	@Nullable BumpType getBumpType();
	@Nullable Identifier getStompTypeID();

	void travelHook(IMarioTravelData data);

	@NotNull List<TransitionDefinition> getBasicTransitions();
	@NotNull List<TransitionDefinition> getInputTransitions();
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions();

	@NotNull Set<TransitionInjectionDefinition> getTransitionInjections();
}
