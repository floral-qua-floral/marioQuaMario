package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import net.minecraft.util.Identifier;

import java.util.EnumSet;
import java.util.function.BiFunction;

public enum AnimationFlag {
	NO_RIGHT_ARM_SWING,
	NO_LEFT_ARM_SWING,
	NO_RIGHT_LEG_SWING,
	NO_LEFT_LEG_SWING,

	USE_RADIANS, // technically more performant i guess but god almighty i don't WANNA

//	CONTINUOUSLY_REEVALUATE_EPHEMERAL,

	NOT_INTERPOLATED;

	public static final EnumSet<AnimationFlag> NO_SWING_ARMS = EnumSet.of(NO_RIGHT_ARM_SWING, NO_LEFT_ARM_SWING);
	public static final EnumSet<AnimationFlag> NO_SWING_LEGS = EnumSet.of(NO_RIGHT_LEG_SWING, NO_LEFT_LEG_SWING);
	public static final EnumSet<AnimationFlag> NO_SWING_LIMBS = EnumSet.of(NO_RIGHT_ARM_SWING, NO_LEFT_ARM_SWING,
			NO_RIGHT_LEG_SWING, NO_LEFT_LEG_SWING);

	public enum Execution {
		MIRROR, // Mirrors the animation horizontally
		DO_NOT_RESET_PROGRESS; // Applying this while entering an animation prevents it from resetting tick progress!

		public static final EnumSet<Execution> NONE = EnumSet.noneOf(Execution.class);
		public static final EnumSet<Execution> ONLY_MIRROR = EnumSet.of(MIRROR);
		public static final BiFunction<CfaAnimatingData, Identifier, EnumSet<Execution>> RANDOMLY_MIRROR =
				(data, prevAnimation) -> data.getPlayer().getRandom().nextBoolean() ? ONLY_MIRROR : NONE;
	}
}
