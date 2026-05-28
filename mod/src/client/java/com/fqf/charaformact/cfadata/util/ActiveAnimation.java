package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.fqf.charaformact.registries.actions.ParsedAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public abstract class ActiveAnimation {
	public final CfaAppearanceData<?> OWNER;
	public final ParsedAnimation ANIMATION;
	public final EnumSet<AnimationFlag.Execution> EXECUTION_FLAGS;

	private final long START_TIME;

	@Contract("_, !null, _, _ -> !null")
	public static @Nullable ActiveAnimation of(
			CfaAppearanceData<?> owner, ParsedAnimation animation,
			AdvancedPosture prevFramePosture, AdvancedArrangement prevFrameTranslation
	) {
		if(animation == null) return null;
		if(animation.FLAGS.contains(AnimationFlag.NOT_INTERPOLATED)) return new NonInterpolated(owner, animation);
//		return new NonInterpolated(owner, animation);
		return new Interpolated(owner, animation, prevFramePosture, prevFrameTranslation);
	}

	private ActiveAnimation(CfaAppearanceData<?> owner, ParsedAnimation animation) {
		this.OWNER = owner;
		this.ANIMATION = animation;

		Identifier previousAnimationID;
		if(this.OWNER.actionAnimation == null) previousAnimationID = null;
		else previousAnimationID = this.OWNER.actionAnimation.ANIMATION.ID;

		this.EXECUTION_FLAGS = animation.getExecutionFlags(owner.DATA, previousAnimationID);

		// If this is the EXACT same animation (happens often with Jump Cap transitions), then don't reset progress
		if(
				!animation.FLAGS.contains(AnimationFlag.CAN_RESET_ON_SELF)
				&& this.OWNER.actionAnimation != null
				&& this.OWNER.actionAnimation.ANIMATION == animation
		)
			this.EXECUTION_FLAGS.add(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS);

		if(this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS) && owner.actionAnimation != null) {
			this.START_TIME = owner.actionAnimation.START_TIME;
			this.EXECUTION_FLAGS.clear();
			this.EXECUTION_FLAGS.addAll(owner.actionAnimation.EXECUTION_FLAGS); // copy previous execution flags (mirroring)
			this.EXECUTION_FLAGS.add(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS); // preserve this one too, even if prev didn't have it
		}
		else this.START_TIME = this.getCurrentTime();
	}

	protected long getCurrentTime() {
		return this.OWNER.PLAYER.getWorld().getTime();
	}

	private float getAnimationTime(long worldTime, float tickDelta) {
		return (worldTime - this.START_TIME) + tickDelta;
	}

	protected void calculateModelArrangement(AdvancedArrangement mutate, long worldTime, float tickDelta) {
		this.ANIMATION.arrangeModel(mutate, this.OWNER.DATA, this.getAnimationTime(worldTime, tickDelta));
		if(!this.ANIMATION.FLAGS.contains(AnimationFlag.USE_RADIANS)) mutate.multiplyAngles(MathHelper.RADIANS_PER_DEGREE);
		if(this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.MIRROR)) mutate.fullyMirror();
	}

	protected void calculatePostureMutations(AdvancedPosture mutate, long worldTime, float tickDelta) {
		boolean mirroring = this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.MIRROR);
		if(mirroring) mutate.fullyMirror();
		if(this.ANIMATION.USE_DEGREES) mutate.toDegrees();
		this.ANIMATION.mutate(mutate, this.OWNER.DATA, this.getAnimationTime(worldTime, tickDelta));
		if(this.ANIMATION.USE_DEGREES) mutate.toRadians();
		if(mirroring) mutate.fullyMirror();
	}

	public abstract void mutateModelArrangement(AdvancedArrangement mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation);

	public abstract void mutatePosture(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation);

	public static class Interpolated extends ActiveAnimation {
		boolean isFirstTickOfPosturing, isFirstTickOfArranging;
		public AdvancedPosture fromPosture, toPosture;
		public AdvancedArrangement fromModelArrangement, toModelArrangement;

		public Interpolated(
				CfaAppearanceData<?> owner, ParsedAnimation animation,
				AdvancedPosture prevFramePosture, AdvancedArrangement prevFrameTranslation
		) {
			super(owner, animation);
			if(this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS) && this.OWNER.actionAnimation != null) {
				if(this.OWNER.actionAnimation instanceof Interpolated prevInterpolatedAction) {
					this.isFirstTickOfPosturing = prevInterpolatedAction.isFirstTickOfPosturing;
					this.fromPosture = prevInterpolatedAction.fromPosture;
					this.toPosture = prevInterpolatedAction.toPosture;
					this.fromModelArrangement = prevInterpolatedAction.fromModelArrangement;
					this.toModelArrangement = prevInterpolatedAction.toModelArrangement;
					return;
				}
			}
			this.isFirstTickOfPosturing = true;
			this.isFirstTickOfArranging = true;
			this.fromPosture = prevFramePosture;
			this.fromModelArrangement = prevFrameTranslation;
		}

		@Override
		public void mutateModelArrangement(AdvancedArrangement mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation) {
			if(isFirstOfTick || this.toModelArrangement == null) {
				if(this.isFirstTickOfArranging) this.isFirstTickOfArranging = false;
				else this.fromModelArrangement = this.toModelArrangement;

				this.toModelArrangement = new AdvancedArrangement();
				// Theoretically we should copy the data from mutate to interpolateToTranslation before calculating, but...
				// it'll always be 0s across the board, anyways.
				this.calculateModelArrangement(this.toModelArrangement, worldTime, tickDelta);
			}

			if(forceWrappedInterpolation || this.ANIMATION.FLAGS.contains(AnimationFlag.ALWAYS_WRAP_ANGLES))
				mutate.wrappedLerpRadians(tickDelta, this.fromModelArrangement, this.toModelArrangement);
			else
				mutate.lerp(tickDelta, this.fromModelArrangement, this.toModelArrangement);
		}

		@Override
		public void mutatePosture(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation) {
			if(isFirstOfTick || this.toPosture == null) {
				if(this.isFirstTickOfPosturing) this.isFirstTickOfPosturing = false;
				else this.fromPosture = this.toPosture;

				this.toPosture = AdvancedPosture.from(mutate);
				this.calculatePostureMutations(this.toPosture, worldTime, tickDelta);
			}

			if(forceWrappedInterpolation || this.ANIMATION.FLAGS.contains(AnimationFlag.ALWAYS_WRAP_ANGLES))
				mutate.wrappedLerp(tickDelta, this.fromPosture, this.toPosture);
			else
				mutate.lerp(tickDelta, this.fromPosture, this.toPosture);
		}
	}

	public static class NonInterpolated extends ActiveAnimation {
		private NonInterpolated(CfaAppearanceData<?> owner, ParsedAnimation animation) {
			super(owner, animation);
		}

		@Override
		public void mutateModelArrangement(AdvancedArrangement mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation) {
			this.calculateModelArrangement(mutate, worldTime, tickDelta);
		}

		@Override
		public void mutatePosture(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation) {
			this.calculatePostureMutations(mutate, worldTime, tickDelta);
		}
	}
}
