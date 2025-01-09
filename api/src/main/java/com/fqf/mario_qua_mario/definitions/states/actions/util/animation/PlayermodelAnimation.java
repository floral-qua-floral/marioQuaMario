package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import org.jetbrains.annotations.Nullable;

public record PlayermodelAnimation(
		@Nullable Arrangement.Mutator wholeMutator,
		@Nullable BodyPartAnimation headAnimation,
		@Nullable BodyPartAnimation torsoAnimation,

		@Nullable LimbAnimation rightArmAnimation,
		@Nullable LimbAnimation leftArmAnimation,

		@Nullable LimbAnimation rightLegAnimation,
		@Nullable LimbAnimation leftLegAnimation,

		@Nullable BodyPartAnimation capeAnimation
) {
}
