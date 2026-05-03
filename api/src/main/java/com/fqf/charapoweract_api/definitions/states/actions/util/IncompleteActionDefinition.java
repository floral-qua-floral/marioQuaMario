package com.fqf.charapoweract_api.definitions.states.actions.util;

import com.fqf.charapoweract_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.PlayermodelAnimation;
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
	
	@Nullable BappingRule getBappingRule();
	@Nullable Identifier getCollisionAttackTypeID();

	@NotNull Set<TransitionInjectionDefinition> getTransitionInjections();
}
