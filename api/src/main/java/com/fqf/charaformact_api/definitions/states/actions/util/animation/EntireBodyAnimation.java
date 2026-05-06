package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import org.jetbrains.annotations.NotNull;

public record EntireBodyAnimation(float pivotHeightFactor, boolean counterRotateHead, @NotNull Arrangement.Mutator mutator
)
		implements PlayermodelAnimation.MutatorContainer {
}
