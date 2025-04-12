package com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation;

import org.jetbrains.annotations.NotNull;

public record BodyPartAnimation(@NotNull Arrangement.Mutator mutator)
		implements PlayermodelAnimation.MutatorContainer {

}
