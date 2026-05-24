package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.util.Easing;
import it.unimi.dsi.fastutil.floats.FloatObjectImmutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface AnimationHelper {
	float interpolateKeyframes(float progress, float first, float second, float... more);

	float easeKeyframes(float progress, float start, List<FloatObjectImmutablePair<Easing>> keyframes);

	float sequencedEase(float progress, Easing first, Easing second, Easing... more);

	@Nullable WallboundActionDefinition.WallInfo getWallInfo(CfaReadableMotionData data);

	void symmetricallyAnimate(Posture posture, Arrangement rightPart, Consumer<Arrangement> animator);
	void symmetricallyAnimate(Posture posture, Arrangement rightPart, SymmetricalAnimator animator);

	void multiAnimate(Consumer<Arrangement> animator, Arrangement... parts);

	@FunctionalInterface
	interface SymmetricalAnimator {
		void animate(Arrangement arrangement, boolean isLeft, int leftFactor);
	}

	@FunctionalInterface
	interface SymmetricalAnimatorInteger {
		void animate(Arrangement arrangement, int isLeft);
	}
}
