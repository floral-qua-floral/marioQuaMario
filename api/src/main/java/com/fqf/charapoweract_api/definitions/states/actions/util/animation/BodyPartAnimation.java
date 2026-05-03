package com.fqf.charapoweract_api.definitions.states.actions.util.animation;

import org.jetbrains.annotations.NotNull;

public record BodyPartAnimation(@NotNull Arrangement.Mutator mutator)
		implements PlayermodelAnimation.MutatorContainer {

}
