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
			ArrangementMutator modelArranger,
			PostureMutator postureMutator
	) {
		return new AnimationDefinitionImpl(id, flags, executionFlagger, modelArranger, postureMutator);
	}

	public static AnimationDefinition of(
			EnumSet<AnimationFlag> flags,
			PostureMutator mutator
	) {
		return of(null, flags, null, null, mutator);
	}

	public static AnimationDefinition of(
			EnumSet<AnimationFlag> flags,
			ArrangementMutator modelArranger,
			PostureMutator mutator
	) {
		return of(null, flags, null, modelArranger, mutator);
	}

	public static AnimationDefinition of(
			ArrangementMutator modelArranger,
			PostureMutator mutator
	) {
		return of(null, null, null, modelArranger, mutator);
	}

	public static AnimationDefinition of(
			EnumSet<AnimationFlag> flags,
			BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagger,
			PostureMutator mutator
	) {
		return of(null, flags, executionFlagger, null, mutator);
	}

	public static AnimationDefinition of(
			EnumSet<AnimationFlag> flags,
			BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagger,
			ArrangementMutator modelArranger,
			PostureMutator mutator
	) {
		return of(null, flags, executionFlagger, modelArranger, mutator);
	}

	public static AnimationDefinition of(
			BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagger,
			PostureMutator mutator
	) {
		return of(null, null, executionFlagger, null, mutator);
	}

	public static AnimationDefinition of(PostureMutator mutator) {
		return of(null, null, null, null, mutator);
	}

	public static AnimationDefinition layerModelArranger(AnimationDefinition animation, ArrangementMutator modelArranger) {
		return of(animation.getID(), animation.defineFlags().clone(), animation::chooseExecutionFlags,
				(arrangement, data, animationTime, helper) -> {
						animation.arrangeModel(arrangement, data, animationTime, helper);
						modelArranger.mutateArrangement(arrangement, data, animationTime, helper);
				}, animation::mutatePosture
		);
	}

	public static AnimationDefinition layerPostureMutator(AnimationDefinition animation, PostureMutator mutator) {
		return of(animation.getID(), animation.defineFlags().clone(), animation::chooseExecutionFlags, animation::arrangeModel,
				(posture, data, animationTime, helper) -> {
						animation.mutatePosture(posture, data, animationTime, helper);
						mutator.mutatePosture(posture, data, animationTime, helper);
				}
		);
	}

	public abstract @Nullable Identifier getID(); // Optional. This is only here so it may be used by chooseExecutionFlags.
	public abstract @NotNull EnumSet<AnimationFlag> defineFlags();

	public abstract @NotNull EnumSet<AnimationFlag.Execution> chooseExecutionFlags(CfaAnimatingData data, @Nullable Identifier prevAnimationID);

	public abstract void arrangeModel(Arrangement arrangement, CfaAnimatingData data, float animationTime, AnimationHelper helper);

	public abstract void mutatePosture(Posture posture, CfaAnimatingData data, float animationTime, AnimationHelper helper);

	private static class AnimationDefinitionImpl extends AnimationDefinition {
		private final @Nullable Identifier ID;
		private final @NotNull EnumSet<AnimationFlag> FLAGS;
		private final @NotNull BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> EXECUTION_FLAGGER;
		private final @NotNull ArrangementMutator MODEL_ARRANGER;
		private final @NotNull PostureMutator POSTURE_MUTATOR;

		private static final ArrangementMutator EMPTY_MUTATOR = (arrangement, data, animationTime, helper) -> {};
		private static final EnumSet<AnimationFlag> EMPTY_FLAGS = EnumSet.noneOf(AnimationFlag.class);
		private static final EnumSet<AnimationFlag.Execution> EMPTY_EXECUTION = EnumSet.noneOf(AnimationFlag.Execution.class);
		private static final BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> EMPTY_FLAGGER =
				(data, prevAnimation) -> EMPTY_EXECUTION;

		private AnimationDefinitionImpl(
				@Nullable Identifier id,
				@Nullable EnumSet<AnimationFlag> flags,
				@Nullable BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagChooser,
				@Nullable ArrangementMutator modelArranger,
				@NotNull PostureMutator postureMutator
		) {
			this.ID = id;
			this.FLAGS = Objects.requireNonNullElse(flags, EMPTY_FLAGS);
			this.EXECUTION_FLAGGER = Objects.requireNonNullElse(executionFlagChooser, EMPTY_FLAGGER);
			this.MODEL_ARRANGER = Objects.requireNonNullElse(modelArranger, EMPTY_MUTATOR);
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
		@Override public void arrangeModel(Arrangement arrangement, CfaAnimatingData data, float animationTime, AnimationHelper helper) {
			this.MODEL_ARRANGER.mutateArrangement(arrangement, data, animationTime, helper);
		}
		@Override public void mutatePosture(Posture posture, CfaAnimatingData data, float animationTime, AnimationHelper helper) {
			this.POSTURE_MUTATOR.mutatePosture(posture, data, animationTime, helper);
		}

		public AnimationDefinition variate(
				@Nullable Identifier id,
				@Nullable EnumSet<AnimationFlag> flags,
				@Nullable BiFunction<CfaAnimatingData, Identifier, EnumSet<AnimationFlag.Execution>> executionFlagChooser,
				@Nullable ArrangementMutator translationMutator,
				@Nullable PostureMutator postureMutator
		) {
			return AnimationDefinition.of(
					id == null ? this.ID : id,
					flags == null ? this.FLAGS : flags,
					executionFlagChooser == null ? this.EXECUTION_FLAGGER : executionFlagChooser,
					translationMutator == null ? this.MODEL_ARRANGER : translationMutator,
					postureMutator == null ? this.POSTURE_MUTATOR : postureMutator
			);
		}
	}

	@FunctionalInterface
	public interface ArrangementMutator {
		void mutateArrangement(Arrangement arrangement, CfaAnimatingData data, float animationTime, AnimationHelper helper);
	}

	@FunctionalInterface
	public interface PostureMutator {
		void mutatePosture(Posture posture, CfaAnimatingData data, float animationTime, AnimationHelper helper);
	}
}
