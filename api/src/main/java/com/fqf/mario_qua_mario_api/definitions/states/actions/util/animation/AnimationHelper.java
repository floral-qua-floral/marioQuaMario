package com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario_api.util.Easing;
import net.minecraft.util.Pair;

import java.util.List;

public interface AnimationHelper {
	float interpolateKeyframes(float progress, float first, float second, float... more);

	float easeKeyframes(float progress, float start, List<Pair<Float, Easing>> keyframes);

	float sequencedEase(float progress, Easing first, Easing second, Easing... more);
}
