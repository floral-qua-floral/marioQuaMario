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

	/**
	 * @param posture The entire posture being mutated.
	 * @param rightPart The right-sided instance of the two body parts being animated. For example, if you're
	 *                  symmetrically animating the arms, you would give <code>posture.RIGHT_ARM</code> for this
	 *                  parameter.
	 * @param animator The lambda method for animating the part. This will be called twice, once on the right limb and
	 *                 then a second time on the left limb. Important: In both cases, the Arrangement it will receive
	 *                 will seem to be right-sided! Always handle it as if it really is right-sided. Changes made to
	 *                 the left limb will be mirrored automatically. The purpose of the second version, which uses
	 *                 DualPartAnimator, is if you want to add extra logic that breaks the symmetry. When used in this
	 *                 method, DualPartAnimator's sideFactor will be ZERO for the right limb, and ONE for the left limb.
	 */
	void symmetricallyAnimate(Posture posture, Arrangement rightPart, Consumer<Arrangement> animator);
	void symmetricallyAnimate(Posture posture, Arrangement rightPart, DualPartAnimator animator);

	/**
	 * Unlike with the symmetricallyAnimate methods, no automatic mirroring will occur. Important: The DualPartAnimator's
	 * sideFactor will be ONE for the right limb and NEGATIVE ONE for the left limb! This is very different behavior
	 * than how symmetricallyAnimate uses the DualPartAnimator, so keep it in mind.
	 */
	void asymmetricallyAnimate(Arrangement rightPart, Arrangement leftPart, DualPartAnimator animator);

	void multiAnimate(Consumer<Arrangement> animator, Arrangement... parts);

	@FunctionalInterface
	interface DualPartAnimator {
		/**
		 * @param arrangement The arrangement representing whichever part is being mutated.
		 * @param isLeft True if the arrangement is the left-sided limb, false otherwise.
		 * @param sideFactor Behavior changes depending on how the DualPartAnimator is being used! See
		 *                   symmetricallyAnimate and asymmetricallyAnimate javadocs above.
		 */
		void animate(Arrangement arrangement, boolean isLeft, int sideFactor);
	}
}
