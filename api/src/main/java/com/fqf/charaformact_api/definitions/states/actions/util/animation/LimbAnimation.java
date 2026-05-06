package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import org.jetbrains.annotations.Nullable;

public record LimbAnimation(boolean shouldSwingWithMovement, @Nullable Arrangement.Mutator mutator)
		implements PlayermodelAnimation.MutatorContainer {

}
