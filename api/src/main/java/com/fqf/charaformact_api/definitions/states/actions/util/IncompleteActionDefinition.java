package com.fqf.charaformact_api.definitions.states.actions.util;

import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IncompleteActionDefinition extends AttackInterceptingStateDefinition {
	default @Nullable AnimationDefinition getAnimation() {
		return null;
	}
	default @Nullable PiecemealPlayermodelAnimation getOldAnimation(AnimationHelper helper) {
		return null;
	}
	@Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper);
	@NotNull SlidingStatus getSlidingStatus();

	@NotNull SneakingRule getSneakingRule();
	@NotNull SprintingRule getSprintingRule();
	
	@Nullable BappingRule getBappingRule();
	@Nullable Identifier getCollisionAttackTypeID();

	@NotNull Set<TransitionInjectionDefinition> getTransitionInjections();
}
