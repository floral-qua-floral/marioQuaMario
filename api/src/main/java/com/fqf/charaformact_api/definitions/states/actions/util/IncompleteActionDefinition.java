package com.fqf.charaformact_api.definitions.states.actions.util;

import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IncompleteActionDefinition extends AttackInterceptingStateDefinition {
	default float defineHitboxWidth() {
		return 0.6F;
	}
	default float defineHitboxHeight() {
		return 1.8F;
	}
	default float defineHitboxEyeHeight() {
		return Math.max(this.defineHitboxHeight() / 2, this.defineHitboxHeight() - 0.18F);
	}

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
