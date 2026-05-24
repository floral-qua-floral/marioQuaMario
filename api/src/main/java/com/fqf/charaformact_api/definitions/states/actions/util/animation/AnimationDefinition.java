package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Animates the player by mutating a Posture object.
 * Please feel free to extend this class to your heart's content. However, a default implementation is provided for your
 * convenience.
 */
public abstract class AnimationDefinition {
	public static AnimationDefinition of(
			Identifier id,
			EnumSet<AnimationFlag> flags,
			BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagger,
			PostureMutator mutator
	) {
		return new AnimationDefinitionImpl(id, flags, executionFlagger, mutator);
	}

	public static AnimationDefinition of(
			EnumSet<AnimationFlag> flags,
			PostureMutator mutator
	) {
		return of(null, flags, null, mutator);
	}

	public static AnimationDefinition of(
			EnumSet<AnimationFlag> flags,
			BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagger,
			PostureMutator mutator
	) {
		return of(null, flags, executionFlagger, mutator);
	}

	public static AnimationDefinition of(
			BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagger,
			PostureMutator mutator
	) {
		return of(null, null, executionFlagger, mutator);
	}

	public static AnimationDefinition of(PostureMutator mutator) {
		return of(null, null, null, mutator);
	}

	/**
	 * Optional. This is only here so it may be used by chooseExecutionFlags.
	 * @return The ID of this animation.
	 */
	public abstract @Nullable Identifier getID();
	public abstract @NotNull EnumSet<AnimationFlag> defineFlags();

	public abstract @NotNull EnumSet<AnimationFlag.Execution> chooseExecutionFlags(CfaAnimatingData data, @Nullable Identifier prevAnimationID);

	public abstract void mutatePosture(Posture posture, CfaAnimatingData data, float animationTime, AnimationHelper helper);


	private static class AnimationDefinitionImpl extends AnimationDefinition {
		private final @Nullable Identifier ID;
		private final @NotNull EnumSet<AnimationFlag> FLAGS;
		private final @NotNull BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> EXECUTION_FLAGGER;
		private final @NotNull PostureMutator POSTURE_MUTATOR;

		private static final EnumSet<AnimationFlag> EMPTY_FLAGS = EnumSet.noneOf(AnimationFlag.class);
		private static final EnumSet<AnimationFlag.Execution> EMPTY_EXECUTION = EnumSet.noneOf(AnimationFlag.Execution.class);
		private static final BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> EMPTY_FLAGGER =
				(data, prevAnimation) -> EMPTY_EXECUTION;

		private AnimationDefinitionImpl(@Nullable Identifier id, @Nullable EnumSet<AnimationFlag> flags, @Nullable BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> ephemeralFlagChooser, @NotNull PostureMutator postureMutator) {
			this.ID = id;
			this.FLAGS = Objects.requireNonNullElse(flags, EMPTY_FLAGS);
			this.EXECUTION_FLAGGER = Objects.requireNonNullElse(ephemeralFlagChooser, EMPTY_FLAGGER);
			this.POSTURE_MUTATOR = Objects.requireNonNull(postureMutator, "Why are you trying to make an animation that does nothing??");
		}

		@Override public @Nullable Identifier getID() {
			return this.ID;
		}
		@Override public @NotNull EnumSet<AnimationFlag> defineFlags() {
			return this.FLAGS;
		}
		@Override public @NotNull EnumSet<AnimationFlag.Execution> chooseExecutionFlags(CfaAnimatingData data, Identifier previousAnimation) {
			return this.EXECUTION_FLAGGER.apply(data, previousAnimation);
		}
		@Override public void mutatePosture(Posture posture, CfaAnimatingData data, float animationTime, AnimationHelper helper) {
			this.POSTURE_MUTATOR.mutatePosture(posture, data, animationTime, helper);
		}
	}

	@FunctionalInterface
	public interface PostureMutator {
		void mutatePosture(Posture posture, CfaAnimatingData data, float animationTime, AnimationHelper helper);
	}
}
