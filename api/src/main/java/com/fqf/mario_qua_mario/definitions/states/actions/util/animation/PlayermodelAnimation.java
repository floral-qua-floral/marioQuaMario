package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param mirroringEvaluator If this returns true, then the animation will be mirrored. This will automatically apply
 *                           the limb animators to the opposite limb from usual (i.e. the right leg animator will apply
 *                           to the left leg), and invert any changes made to any part's X position and yaw. If null,
 *                           then the animation will never be mirrored - it's equivalent to giving it a function that
 *                           always returns false.
 * @param progressHandler Responsible for handling the progression of the animation. See its javadoc for info. If null,
 *                        then every body part animation will just be given 1F as its progress.
 * @param entireBodyAnimation If present, will be used to rotate and/or translate the entire player renderer. See its
 *                            javadoc for info. Mario's head will automatically rotate to try and correct for any Pitch
 *                            & Yaw adjustments made here, so that his aim remains steady even as his model rotates.
 * @param headAnimation If present, will be used to rotate and/or translate Mario's head. See the BodyPartAnimation
 *                      javadoc for info.
 * @param torsoAnimation If present, will be used to rotate and/or translate Mario's head.
 * @param rightArmAnimation If present, will be used to rotate and/or translate Mario's right arm (left if the animation
 *                          is mirrored). See LimbAnimation javadoc for info.
 * @param leftArmAnimation If present, will be used to rotate and/or translate Mario's left arm.
 * @param rightLegAnimation If present, will be used to rotate and/or translate Mario's right leg (left if the animation
 *                          is mirrored).
 * @param leftLegAnimation If present, will be used to rotate and/or translate Mario's left leg.
 * @param capeAnimation Primarily used for Raccoon Mario's tail. Using positional adjustments to lower the pivot down to
 *                      Mario's waist is not necessary; the Raccoon Mario playermodel handles that already.
 */
public record PlayermodelAnimation(
		@Nullable PlayermodelAnimation.MirroringEvaluator mirroringEvaluator,
		@Nullable ProgressHandler progressHandler,

		@Nullable EntireBodyAnimation entireBodyAnimation,
		@Nullable BodyPartAnimation headAnimation,
		@Nullable BodyPartAnimation torsoAnimation,

		@Nullable LimbAnimation rightArmAnimation,
		@Nullable LimbAnimation leftArmAnimation,

		@Nullable LimbAnimation rightLegAnimation,
		@Nullable LimbAnimation leftLegAnimation,

		@Nullable BodyPartAnimation capeAnimation
) {
	public PlayermodelAnimation variate(
			@Nullable PlayermodelAnimation.MirroringEvaluator mirroringEvaluator,
			@Nullable ProgressHandler progressHandler,

			@Nullable EntireBodyAnimation entireBodyAnimation,
			@Nullable BodyPartAnimation headAnimation,
			@Nullable BodyPartAnimation torsoAnimation,

			@Nullable LimbAnimation rightArmAnimation,
			@Nullable LimbAnimation leftArmAnimation,

			@Nullable LimbAnimation rightLegAnimation,
			@Nullable LimbAnimation leftLegAnimation,

			@Nullable BodyPartAnimation capeAnimation
	) {
		return new PlayermodelAnimation(
				mirroringEvaluator == null ? this.mirroringEvaluator : mirroringEvaluator,
				progressHandler == null ? this.progressHandler : progressHandler,

				entireBodyAnimation == null ? this.entireBodyAnimation : entireBodyAnimation,
				headAnimation == null ? this.headAnimation : headAnimation,
				torsoAnimation == null ? this.torsoAnimation : torsoAnimation,

				rightArmAnimation == null ? this.rightArmAnimation : rightArmAnimation,
				leftArmAnimation == null ? this.leftArmAnimation : leftArmAnimation,

				rightLegAnimation == null ? this.rightLegAnimation : rightLegAnimation,
				leftLegAnimation == null ? this.leftLegAnimation : leftLegAnimation,

				capeAnimation == null ? this.capeAnimation : capeAnimation
		);
	}

	@FunctionalInterface
	public interface MirroringEvaluator {
		/**
		 * @param data The data for what player we're checking animation mirroring for.
		 * @param rightArmBusy Whether Mario's right arm is currently being used in a way that prohibits animation -
		 *                     for example, throwing a Trident.
		 * @param leftArmBusy Whether Mario's left arm is currently being used in a way that prohibits animation.
		 * @param headRelativeYaw The difference between the Yaw of Mario's head and the Yaw of his body. If it's
		 *                        positive, he's looking left.
		 * @return Whether the animation should be automatically mirrored.
		 */
		boolean shouldMirror(IMarioReadableMotionData data, boolean rightArmBusy, boolean leftArmBusy, float headRelativeYaw);
	}
}
