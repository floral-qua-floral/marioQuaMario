package com.floralquafloral.util;

import static java.lang.Math.*;

// https://easings.net/
// https://gist.github.com/dev-hydrogen/21a66f83f0386123e0c0acf107254843
public enum MixedEasing {
	SINE(x -> -(cos(PI * x) - 1) / 2),
	QUAD(x -> x < 0.5 ? 2 * x * x : 1 - pow(-2 * x + 2, 2) / 2),
	CUBIC(x -> x < 0.5 ? 4 * x * x * x : 1 - pow(-2 * x + 2, 3) / 2),
	QUART(x -> x < 0.5 ? 8 * x * x * x * x : 1 - pow(-2 * x + 2, 4) / 2),
	QUINT(x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - pow(-2 * x + 2, 5) / 2),
	EXPONENTIAL(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? pow(2, 20 * x - 10) / 2 : (2 - pow(2, -20 * x + 10)) / 2),
	CIRCLE(x -> x < 0.5 ? (1 - sqrt(1 - pow(2 * x, 2))) / 2 : (sqrt(1 - pow(-2 * x + 2, 2)) + 1) / 2),
	BACK(x -> x < 0.5 ? (pow(2 * x, 2) * ((1.70158 * 1.525 + 1) * 2 * x - 1.70158 * 1.525)) / 2 : (pow(2 * x - 2, 2) * ((1.70158 * 1.525 + 1) * (x * 2 - 2) + 1.70158 * 1.525) + 2) / 2),
	ELASTIC(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? -(pow(2, 20 * x - 10) * sin((20 * x - 11.125) * ((2 * PI) / 4.5))) / 2 : (pow(2, -20 * x + 10) * sin((20 * x - 11.125) * ((2 * PI) / 4.5))) / 2 + 1);

	@FunctionalInterface
	private interface EasingFunction {
		double ease(double x);
	}

	final EasingFunction FUNCTION;
	MixedEasing(EasingFunction function) {
		this.FUNCTION = function;
	}

	public static float mixedEase(float x, MixedEasing first, MixedEasing second) {
//		return (float) (x < 0.5 ? first.FUNCTION.ease(x) : second.FUNCTION.ease(x));
		return x * (float) first.FUNCTION.ease(x) + (1 - x) * (float) second.FUNCTION.ease(x);
	}
}