package com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import org.jetbrains.annotations.NotNull;

public record EntireBodyAnimation(float pivotHeightFactor, boolean counterRotateHead, @NotNull Arrangement.Mutator mutator
)
		implements PiecemealPlayermodelAnimation.MutatorContainer {
}
