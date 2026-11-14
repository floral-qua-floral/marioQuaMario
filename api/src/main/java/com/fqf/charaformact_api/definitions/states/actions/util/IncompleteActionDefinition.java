package com.fqf.charaformact_api.definitions.states.actions.util;

import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface IncompleteActionDefinition extends AttackInterceptingStateDefinition {
	default float defineHitboxWidth() {
		return 0.6F;
	}
	default float defineHitboxHeight() {
		return 1.8F; // Vanilla sneaking: 1.5
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

	default void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {

	}

	default @Nullable BappingRule defineBappingRule() {
		return null;
	}
	default void accumulateCollisionAttacks(ImmutableSet.Builder<Identifier> builder) {

	}
}
