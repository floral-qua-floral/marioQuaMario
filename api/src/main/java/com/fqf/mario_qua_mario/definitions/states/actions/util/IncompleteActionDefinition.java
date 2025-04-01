package com.fqf.mario_qua_mario.definitions.states.actions.util;

import com.fqf.mario_qua_mario.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IncompleteActionDefinition extends AttackInterceptingStateDefinition {
	@Nullable PlayermodelAnimation getAnimation(AnimationHelper helper);
	@Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper);
	@NotNull SlidingStatus getSlidingStatus();

	@NotNull SneakingRule getSneakingRule();
	@NotNull SprintingRule getSprintingRule();
	
	@Nullable BumpType getBumpType();
	@Nullable Identifier getStompTypeID();

	@NotNull Set<TransitionInjectionDefinition> getTransitionInjections();
}
