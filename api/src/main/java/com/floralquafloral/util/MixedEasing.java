package com.floralquafloral.util;

import static java.lang.Math.*;

/**
 * Used for "mixed easing" - easing that uses one kind of easing function for the first half, then a different one for
 * the second half. Intended for use with Camera Animations.
 */
public enum MixedEasing {
	SINE(Easings::easeInOutSine),
	QUAD(Easings::easeInOutQuad),
	CUBIC(Easings::easeInOutCubic),
	QUART(Easings::easeInOutQuart),
	QUINT(Easings::easeInOutQuint),
	EXPONENTIAL(Easings::easeInOutExpo),
	CIRCLE(Easings::easeInOutCirc),
	BACK(Easings::easeInOutBack),
	ELASTIC(Easings::easeInOutElastic);

	@FunctionalInterface
	private interface EasingFunction {
		float ease(float x);
	}

	final EasingFunction FUNCTION;
	MixedEasing(EasingFunction function) {
		this.FUNCTION = function;
	}

	public static float mixedEase(float x, MixedEasing first, MixedEasing second) {
		return x * first.FUNCTION.ease(x) + (1 - x) * second.FUNCTION.ease(x);
	}
}