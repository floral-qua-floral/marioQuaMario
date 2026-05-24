package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.fqf.charaformact.registries.actions.ParsedAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public abstract class ActiveAnimation {
	public final CfaAppearanceData<?> OWNER;
	public final ParsedAnimation ANIMATION;
	public final EnumSet<AnimationFlag.Execution> EXECUTION_FLAGS;

	private final long START_TIME;

	@Contract("_, !null, _ -> !null")
	public static @Nullable ActiveAnimation of(CfaAppearanceData<?> owner, ParsedAnimation animation, AdvancedPosture prevTickPosture) {
		if(animation == null) return null;
		if(animation.FLAGS.contains(AnimationFlag.NOT_INTERPOLATED)) return new NonInterpolated(owner, animation);
//		return new NonInterpolated(owner, animation);
		return new Interpolated(owner, animation, prevTickPosture);
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
		) {
			CharaFormAct.LOGGER.info("Transitioning from an animation into the exact same animation! We won't reset progress!");
			this.EXECUTION_FLAGS.add(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS);
		}

		if(this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS) && owner.actionAnimation != null) {
			this.START_TIME = owner.actionAnimation.START_TIME;
			this.EXECUTION_FLAGS.clear();
			this.EXECUTION_FLAGS.addAll(owner.actionAnimation.EXECUTION_FLAGS); // copy previous execution flags (mirroring)
			this.EXECUTION_FLAGS.add(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS); // preserve this one too, even if prev didn't have it
		}
		else this.START_TIME = this.getCurrentTime();

		CharaFormAct.LOGGER.info("Playing an animation with start time {}", this.START_TIME);
	}

	protected long getCurrentTime() {
		return this.OWNER.PLAYER.getWorld().getTime();
	}

	public void calculateMutations(AdvancedPosture mutate, long worldTime, float tickDelta) {
		boolean mirroring = this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.MIRROR);
		if(mirroring) mutate.fullyMirror();
		if(this.ANIMATION.USE_DEGREES) mutate.toDegrees();
		this.ANIMATION.mutate(mutate, this.OWNER.DATA, (worldTime - this.START_TIME) + tickDelta);
		if(this.ANIMATION.USE_DEGREES) mutate.toRadians();
		if(mirroring) mutate.fullyMirror();
	}

	public abstract void apply(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation);

	public static class Interpolated extends ActiveAnimation {
		boolean isFirstTickOfAnimation;
		public AdvancedPosture interpolateFrom;
		public AdvancedPosture interpolateTo;

		public Interpolated(CfaAppearanceData<?> owner, ParsedAnimation animation, AdvancedPosture prevTickPosture) {
			super(owner, animation);
			if(this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS) && this.OWNER.actionAnimation != null) {
				if(this.OWNER.actionAnimation instanceof Interpolated prevInterpolatedAction) {
					this.isFirstTickOfAnimation = prevInterpolatedAction.isFirstTickOfAnimation;
					this.interpolateFrom = prevInterpolatedAction.interpolateFrom;
					this.interpolateTo = prevInterpolatedAction.interpolateTo;
					return;
				}
			}
			this.isFirstTickOfAnimation = true;
			this.interpolateFrom = prevTickPosture;
		}

		@Override
		public void apply(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation) {
			if(isFirstOfTick || this.interpolateTo == null) {
				if(this.isFirstTickOfAnimation) this.isFirstTickOfAnimation = false;
				else this.interpolateFrom = this.interpolateTo;

				this.interpolateTo = AdvancedPosture.from(mutate);
				this.calculateMutations(this.interpolateTo, worldTime, tickDelta);
			}

			if(forceWrappedInterpolation || this.ANIMATION.FLAGS.contains(AnimationFlag.ALWAYS_WRAP_ANGLES))
				mutate.wrappedLerp(tickDelta, this.interpolateFrom, this.interpolateTo);
			else
				mutate.lerp(tickDelta, this.interpolateFrom, this.interpolateTo);
		}
	}

	public static class NonInterpolated extends ActiveAnimation {
		private NonInterpolated(CfaAppearanceData<?> owner, ParsedAnimation animation) {
			super(owner, animation);
		}

		@Override
		public void apply(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick, boolean forceWrappedInterpolation) {
			this.calculateMutations(mutate, worldTime, tickDelta);
		}
	}
}
