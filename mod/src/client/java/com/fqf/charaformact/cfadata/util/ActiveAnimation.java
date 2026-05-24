package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.fqf.charaformact.registries.actions.ParsedAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public abstract class ActiveAnimation {
	public final CfaAppearanceData<?> OWNER;
	public final ParsedAnimation ANIMATION;
	public final EnumSet<AnimationFlag.Execution> EXECUTION_FLAGS;

	private final long START_TIME;

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

		if(this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS) && owner.actionAnimation != null)
			this.START_TIME = owner.actionAnimation.START_TIME;
		else
			this.START_TIME = this.getCurrentTime();

		if(this.OWNER.actionAnimation != null && this.OWNER.actionAnimation.ANIMATION == animation) {
			// If this is the exact same animation, then force MIRROR to match.
			if(this.OWNER.actionAnimation.EXECUTION_FLAGS.contains(AnimationFlag.Execution.MIRROR))
				this.EXECUTION_FLAGS.add(AnimationFlag.Execution.MIRROR);
			else
				this.EXECUTION_FLAGS.remove(AnimationFlag.Execution.MIRROR);
		}
	}

	protected long getCurrentTime() {
		return this.OWNER.PLAYER.getWorld().getTime();
	}

	public void calculateMutations(AdvancedPosture mutate, long worldTime, float tickDelta) {
		boolean mirroring = this.EXECUTION_FLAGS.contains(AnimationFlag.Execution.MIRROR);
		if(mirroring) mutate.fullyMirror();
		if(this.ANIMATION.USE_DEGREES) mutate.multiplyAllAngles(MathHelper.DEGREES_PER_RADIAN);
		this.ANIMATION.mutate(mutate, this.OWNER.DATA, (worldTime - this.START_TIME) + tickDelta);
		if(this.ANIMATION.USE_DEGREES) mutate.multiplyAllAngles(MathHelper.RADIANS_PER_DEGREE);
		if(mirroring) mutate.fullyMirror();
	}

	public abstract void apply(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick);

	public static class Interpolated extends ActiveAnimation {
		boolean isFirstTickOfAnimation;
		AdvancedPosture interpolateFrom;
		AdvancedPosture interpolateTo;

		public Interpolated(CfaAppearanceData<?> owner, ParsedAnimation animation, AdvancedPosture prevTickPosture) {
			super(owner, animation);
			this.interpolateFrom = prevTickPosture;
			this.isFirstTickOfAnimation = true;
		}

		@Override
		public void apply(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick) {
			if(isFirstOfTick || this.interpolateTo == null) {
				if(this.isFirstTickOfAnimation) this.isFirstTickOfAnimation = false;
				else this.interpolateFrom = this.interpolateTo;

				this.interpolateTo = AdvancedPosture.from(mutate);
				this.calculateMutations(this.interpolateTo, worldTime, tickDelta);
			}

			mutate.lerp(this.interpolateFrom, this.interpolateTo, tickDelta);
		}
	}

	public static class NonInterpolated extends ActiveAnimation {
		private NonInterpolated(CfaAppearanceData<?> owner, ParsedAnimation animation) {
			super(owner, animation);
		}

		@Override
		public void apply(AdvancedPosture mutate, long worldTime, float tickDelta, boolean isFirstOfTick) {
			this.calculateMutations(mutate, worldTime, tickDelta);
		}
	}
}
