package com.floralquafloral.util;

import static java.lang.Math.*;

// https://easings.net/
// https://gist.github.com/dev-hydrogen/21a66f83f0386123e0c0acf107254843
public abstract class Easings {
	public static float OFF(float x) {
		return x;
	}

	private static int START(float x) {
		return x == 1 ? 1 : 0;
	}

	private static int HALF(float x) {
		return x >= 0.5 ? 1 : 0;
	}

	private static int END(float x) {
		return x == 0 ? 0 : 1;
	}

	public static float easeInSine(float x) {
		return 1 - (float) cos(x * PI) / 2;
	}

	public static float easeOutSine(float x) {
		return (float) sin(x * PI / 2);
	}

	public static float easeInOutSine(float x) {
		return -((float) cos(PI * x) - 1) / 2;
	}

	public static float easeInQuad(float x) {
		return x * x;
	}

	public static float easeOutQuad(float x) {
		return 1 - (1 - x) * (1 - x);
	}

	public static float easeInOutQuad(float x) {
		return x < 0.5F ? 2 * x * x : 1 - (float) pow(-2 * x + 2, 2) / 2;
	}

	public static float easeInCubic(float x) {
		return x * x * x;
	}

	public static float easeOutCubic(float x) {
		return 1 - (float) pow(1 - x, 3);
	}

	public static float easeInOutCubic(float x) {
		return x < 0.5F ? 4 * x * x * x : 1 - (float) pow(-2 * x + 2, 3) / 2;
	}

	public static float easeInQuart(float x) {
		return x * x * x * x;
	}

	public static float easeOutQuart(float x) {
		return 1 - (float) pow(1 - x, 4);
	}

	public static float easeInOutQuart(float x) {
		return x < 0.5 ? 8 * x * x * x * x : 1 - (float) pow(-2 * x + 2, 4) / 2;
	}

	public static float easeInQuint(float x) {
		return x * x * x * x * x;
	}

	public static float easeOutQuint(float x) {
		return 1 - (float) pow(1 - x, 5);
	}

	public static float easeInOutQuint(float x) {
		return x < 0.5F ? 16 * x * x * x * x * x : 1 - (float) pow(-2 * x + 2, 5) / 2;
	}

	public static float easeInExpo(float x) {
		return x == 0 ? 0 : (float) pow(2, 10 * x - 10);
	}

	public static float easeOutExpo(float x) {
		return x == 1 ? 1 : 1 - (float) pow(2, -10 * x);
	}

	public static float easeInOutExpo(float x) {
		return x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? (float) pow(2, 20 * x - 10) / 2 : (2 - (float) pow(2, -20 * x + 10)) / 2;
	}

	public static float easeInCirc(float x) {
		return 1 - (float) sqrt(1 - pow(x, 2));
	}

	public static float easeOutCirc(float x) {
		return (float) sqrt(1 - pow(x - 1, 2));
	}

	public static float easeInOutCirc(float x) {
		return x < 0.5F ? (1 - (float) sqrt(1 - pow(2 * x, 2))) / 2 : ((float) sqrt(1 - pow(-2 * x + 2, 2)) + 1) / 2;
	}

	public static float easeInBack(float x) {
		return 2.70158F * x * x * x - 1.70158F * x * x;
	}

	public static float easeOutBack(float x) {
		return 1 + 2.70158F * (float) pow(x - 1, 3) + 1.70158F * (float) pow(x - 1, 2);
	}

	public static float easeInOutBack(float x) {
		return x < 0.5F ? ((float) pow(2 * x, 2) * ((1.70158F * 1.525F + 1) * 2 * x - 1.70158F * 1.525F)) / 2 : ((float) pow(2 * x - 2, 2) * ((1.70158F * 1.525F + 1) * (x * 2 - 2) + 1.70158F * 1.525F) + 2) / 2;
	}

	public static float easeInElastic(float x) {
		return x == 0 ? 0 : x == 1 ? 1 : -(float) pow(2, 10 * x - 10) * (float) sin((x * 10 - 10.75F) * ((float) (2 * PI) / 3));
	}

	public static float easeOutElastic(float x) {
		return x == 0 ? 0 : x == 1 ? 1 : (float) pow(2, -10 * x) * (float) sin((x * 10 - 0.75F) * ((2 * PI) / 3)) + 1;
	}

	public static float easeInOutElastic(float x) {
		return x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? -((float) pow(2, 20 * x - 10) * (float) sin((20 * x - 11.125F) * ((2 * PI) / 4.5))) / 2 : ((float) pow(2, -20 * x + 10) * (float) sin((20 * x - 11.125F) * ((2 * PI) / 4.5))) / 2 + 1;
	}
}