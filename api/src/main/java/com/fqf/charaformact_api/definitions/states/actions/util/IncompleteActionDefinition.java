package com.fqf.charaformact_api.definitions.states.actions.util;

import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IncompleteActionDefinition extends AttackInterceptingStateDefinition {
	default @Nullable AnimationDefinition defineAnimation() {
		return null;
	}
	default @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return null;
	}

	default @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	default @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.ALLOW;
	}
	default @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.ALLOW;
	}
	
	default @Nullable BappingRule defineBappingRule() {
		return null;
	}
	default @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}
}
