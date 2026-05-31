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

	CAN_RESET_ON_SELF, // animation will reset its progress when transitioning into itself, unless given DO_NOT_RESET_PROGRESS execution flag

	NOT_INTERPOLATED, // by default, animations are run once per tick, and the player interpolates between the poses. this disables that

	NO_HEAD_COUNTERROTATION, // disables system that tries to keep the player's head aligned with their real look angle.

	// Maybe one day I should add flags that allow an animation to ignore busy arms? Or cancel busy arm auto-positioning?

	ALWAYS_WRAP_ANGLES; // mostly relevant for interpolation

	public static final EnumSet<AnimationFlag> NO_SWING_ARMS = EnumSet.of(NO_RIGHT_ARM_SWING, NO_LEFT_ARM_SWING);
	public static final EnumSet<AnimationFlag> NO_SWING_LEGS = EnumSet.of(NO_RIGHT_LEG_SWING, NO_LEFT_LEG_SWING);
	public static final EnumSet<AnimationFlag> NO_SWING_LIMBS = EnumSet.of(NO_RIGHT_ARM_SWING, NO_LEFT_ARM_SWING,
			NO_RIGHT_LEG_SWING, NO_LEFT_LEG_SWING);

	public enum Execution {
		MIRROR, // Mirrors the animation horizontally
//		PRESERVE_MIRRORING, // Copies the mirroring status of the previous animation. TODO: Implement???
		DO_NOT_RESET_PROGRESS; // Applying this while entering an animation prevents it from resetting tick progress!

		public static final EnumSet<Execution> NONE = EnumSet.noneOf(Execution.class);
		public static final EnumSet<Execution> ONLY_MIRROR = EnumSet.of(MIRROR);
		public static final BiFunction<CfaAnimatingData, Identifier, EnumSet<Execution>> RANDOMLY_MIRROR =
				(data, prevAnimation) -> data.getPlayer().getRandom().nextBoolean() ? ONLY_MIRROR : NONE;
	}
}
