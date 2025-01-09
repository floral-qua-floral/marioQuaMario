package com.fqf.mario_qua_mario.util;

import net.minecraft.util.math.MathHelper;

import static net.minecraft.util.math.MathHelper.PI;
import static net.minecraft.util.math.MathHelper.sin;
import static net.minecraft.util.math.MathHelper.cos;
import static net.minecraft.util.math.MathHelper.sqrt;

@FunctionalInterface
public interface Easing {
	InOutEasing LINEAR = progress -> progress;

	Easing SNAP_EARLY = x -> x > 0 ? 1 : 0;
	InOutEasing SNAP_HALFWAY = x -> x >= 0.5 ? 1 : 0;
	Easing SNAP_LATE = x -> x == 1 ? 1 : 0;

	Easing SINE_IN = x -> 1 - cos(x * PI / 2);
	InOutEasing SINE_IN_OUT = x -> -(cos(PI * x) - 1) / 2;
	Easing SINE_OUT = x -> sin(x * PI / 2);

	Easing QUAD_IN = x -> x * x;
	InOutEasing QUAD_IN_OUT = x -> x < 0.5F ? 2 * x * x : 1 - QUAD_IN.ease(-2 * x + 2) / 2;
	Easing QUAD_OUT = x -> 1 - (1 - x) * (1 - x);

	Easing CUBIC_IN = x -> x * x * x;
	InOutEasing CUBIC_IN_OUT = x -> x < 0.5F ? 4 * x * x * x : 1 - CUBIC_IN.ease(-2 * x + 2) / 2;
	Easing CUBIC_OUT = x -> 1 - CUBIC_IN.ease(1 - x);

	Easing QUART_IN = x -> x * x * x * x;
	InOutEasing QUART_IN_OUT = x -> x < 0.5F ?  8 * x * x * x * x : 1 - QUART_IN.ease(-2 * x + 2) / 2;
	Easing QUART_OUT = x -> 1 - QUART_IN.ease(1 - x);

	Easing QUINT_IN = x -> x * x * x * x * x;
	InOutEasing QUINT_IN_OUT = x -> x < 0.5F ? 16 * QUINT_IN.ease(x) : QUINT_IN.ease(-2 * x + 2) / 2;
	Easing QUINT_OUT = x -> QUINT_IN.ease(1 - x);

	Easing EXPO_IN = x -> x == 0 ? 0 : pow(2, 10 * x - 10);
	InOutEasing EXPO_IN_OUT = x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? pow(2, 20 * x - 10) / 2 : (2 - pow(2, -20 * x + 10)) / 2;
	Easing EXPO_OUT = x -> x == 1 ? 1 : 1 - pow(2, -10 * x);

	Easing CIRC_IN = x -> 1 - sqrt(1 - pow(x, 2));
	InOutEasing CIRC_IN_OUT = x -> x < 0.5F ? (1 - sqrt(1 - pow(2 * x, 2))) / 2 : (sqrt(1 - pow(-2 * x + 2, 2)) + 1) / 2;
	Easing CIRC_OUT = x -> sqrt(1 - pow(x - 1, 2));

	Easing BACK_IN = x -> 2.70158F * x * x * x - 1.70158F * x * x;
	InOutEasing BACK_IN_OUT = x -> x < 0.5F ? (pow(2 * x, 2) * ((1.70158F * 1.525F + 1) * 2 * x - 1.70158F * 1.525F)) / 2 : (pow(2 * x - 2, 2) * ((1.70158F * 1.525F + 1) * (x * 2 - 2) + 1.70158F * 1.525F) + 2) / 2;
	Easing BACK_OUT = x -> 1 + 2.70158F * pow(x - 1, 3) + 1.70158F * pow(x - 1, 2);

	Easing ELASTIC_IN = x -> x == 0 ? 0 : x == 1 ? 1 : -pow(2, 10 * x - 10) * sin((x * 10 - 10.75F) * ((2 * PI) / 3));
	InOutEasing ELASTIC_IN_OUT = x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? -(pow(2, 20 * x - 10) * sin((20 * x - 11.125F) * ((2 * PI) / 4.5F))) / 2 : (pow(2, -20 * x + 10) * sin((20 * x - 11.125F) * ((2 * PI) / 4.5F))) / 2 + 1;
	Easing ELASTIC_OUT = x -> x == 0 ? 0 : x == 1 ? 1 : pow(2, -10 * x) * sin((x * 10 - 0.75F) * ((2 * PI) / 3)) + 1;


	static Easing mix(InOutEasing in, InOutEasing out) {
		return progress -> mixedEase(in, out, progress);
	}
	static float mixedEase(InOutEasing in, InOutEasing out, float progress) {
		return LINEAR.ease(progress, in.ease(progress), out.ease(progress));
	}
	static float mixedEase(InOutEasing in, InOutEasing out, float progress, float start, float end) {
		return start + ((end - start) * mixedEase(in, out, progress));
	}

	static float clampedRangeToProgress(float x, float min, float max) {
		return MathHelper.clamp((x - min) / (max - min), 0, 1);
	}
	static float clampedRangeToProgress(double x, float min, float max) {
		return clampedRangeToProgress((float) x, min, max);
	}

	float ease(float x);
	default float ease(float progress, float start, float end) {
		return start + ((end - start) * this.ease(progress));
	}

	private static float pow(float a, float b) {
		return (float) Math.pow(a, b);
	}
	interface InOutEasing extends Easing {}
}
