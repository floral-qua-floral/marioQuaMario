package com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import org.jetbrains.annotations.Nullable;

public record LimbAnimation(boolean shouldSwingWithMovement, @Nullable Arrangement.Mutator mutator)
		implements PiecemealPlayermodelAnimation.MutatorContainer {

}
