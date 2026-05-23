package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import java.util.EnumSet;

public enum AnimationFlag {
	NO_RIGHT_ARM_SWING,
	NO_LEFT_ARM_SWING,
	NO_RIGHT_LEG_SWING,
	NO_LEFT_LEG_SWING,

//	CONTINUOUSLY_REEVALUATE_EPHEMERAL,

	NOT_INTERPOLATED;

	public static final EnumSet<AnimationFlag> NO_SWING_ARMS = EnumSet.of(NO_RIGHT_ARM_SWING, NO_LEFT_ARM_SWING);
	public static final EnumSet<AnimationFlag> NO_SWING_LEGS = EnumSet.of(NO_RIGHT_LEG_SWING, NO_LEFT_LEG_SWING);
	public static final EnumSet<AnimationFlag> NO_SWING_LIMBS = EnumSet.of(NO_RIGHT_ARM_SWING, NO_LEFT_ARM_SWING,
			NO_RIGHT_LEG_SWING, NO_LEFT_LEG_SWING);

	public enum Execution {
		MIRROR, // Mirrors the animation horizontally
		DO_NOT_RESET_PROGRESS; // Applying this while entering an animation prevents it from resetting tick progress!
	}
}
