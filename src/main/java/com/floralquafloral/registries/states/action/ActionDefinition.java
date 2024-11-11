package com.floralquafloral.registries.states.action;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.states.MarioStateDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ActionDefinition extends MarioStateDefinition {
	@Nullable String getAnimationName();
	@Nullable CameraAnimationSet getCameraAnimations();

	SneakLegalityRule getSneakLegalityRule();
	SlidingStatus getActionSlidingStatus();
	@Nullable Identifier getStompType();

	void travelHook(MarioTravelData data);

	List<ActionTransitionDefinition> getPreTickTransitions();
	List<ActionTransitionDefinition> getPostTickTransitions();
	List<ActionTransitionDefinition> getPostMoveTransitions();
	List<ActionTransitionInjection> getTransitionInjections();

	class CameraAnimationSet {
		@NotNull public final CameraAnimation AUTHENTIC_ANIMATION;
		@NotNull public final CameraAnimation GENTLE_ANIMATION;
		@Nullable public final CameraAnimation MINIMAL_ANIMATION;

		public CameraAnimationSet(
				@NotNull CameraAnimation authentic,
				@Nullable CameraAnimation gentle,
				@Nullable CameraAnimation minimal
		) {
			this.AUTHENTIC_ANIMATION = authentic;
			this.GENTLE_ANIMATION = gentle == null ? authentic : gentle;
			this.MINIMAL_ANIMATION = minimal;
		}
	}
	class CameraAnimation {
		@FunctionalInterface public interface rotationalOffsetCalculator {
			void setRotationalOffsets(float progress, float[] offsets);
		}

		public final boolean SHOULD_LOOP;
		public final float DURATION_TICKS;
		public final rotationalOffsetCalculator CALCULATOR;

		public CameraAnimation(
				boolean looping,
				float durationSeconds,
				rotationalOffsetCalculator calculator
		) {
			this.SHOULD_LOOP = looping;
			this.DURATION_TICKS = durationSeconds * 20;
			this.CALCULATOR = calculator;
		}
	}

	enum SneakLegalityRule {
		ALLOW(true, false),		// Player can enter the sneaking pose
		PROHIBIT(false, false),	// Player cannot enter the sneaking pose
		SLIP(true, true);		// Player can enter the sneaking pose, but won't clip at ledges

		private final boolean PROHIBIT_SNEAK;
		private final boolean SLIP_OFF_LEDGES;
		SneakLegalityRule(boolean canSneak, boolean slip) {
			PROHIBIT_SNEAK = !canSneak;
			SLIP_OFF_LEDGES = slip;
		}
		public boolean prohibitSneak() {
			return this.PROHIBIT_SNEAK;
		}
		public boolean slipOffLedges() {
			return this.SLIP_OFF_LEDGES;
		}
	}
	enum SlidingStatus {
		SLIDING(false, false, true, true, false, true),					// No view bobbing or footsteps, with sliding sound & block particles
		SKIDDING(false, false, true, true, false, false),				// Same as SLIDING, but the sound doesn't fade with reduced speed
		SLIDING_NO_PARTICLES(false, false, true, false, false, true),	// No view bobbing or footsteps, with sliding sound
		WALL_SLIDING(false, false, false, false, true, true),			// No view bobbing or footsteps, with alternate sliding sound
		SLIDING_SILENT(false, false, false, false, false, true),		// No view bobbing or footsteps.
		NOT_SLIDING_SMOOTH(true, false, false, false, false, true),		// Footsteps, but no view bobbing
		NOT_SLIDING(true, true, false, false, false, true);				// Vanilla view bobbing & footstep sounds

		public final boolean DO_FOOTSTEPS;
		public final boolean DO_VIEW_BOBBING;
		public final boolean DO_SLIDE_SFX;
		public final boolean DO_ALT_SLIDE_SFX;
		public final boolean DO_SLIDE_PARTICLES;
		public final boolean DO_FADING;

		SlidingStatus(boolean doFootsteps, boolean doViewBobbing, boolean doSfx, boolean doParticles, boolean isWall, boolean speedScaling) {
			this.DO_FOOTSTEPS = doFootsteps;
			this.DO_VIEW_BOBBING = doViewBobbing;
			this.DO_SLIDE_SFX = doSfx;
			this.DO_SLIDE_PARTICLES = doParticles;
			this.DO_ALT_SLIDE_SFX = isWall;
			this.DO_FADING = speedScaling;
		}
		public boolean doFootsteps() {
			return this.DO_FOOTSTEPS;
		}
		public boolean doViewBobbing() {
			return this.DO_VIEW_BOBBING;
		}
		public boolean doSlideSfx() {
			return this.DO_SLIDE_SFX;
		}
		public boolean doWallSlideSfx() {
			return this.DO_ALT_SLIDE_SFX;
		}
		public boolean doParticles() {
			return this.DO_SLIDE_PARTICLES;
		}
		public boolean doSpeedScaling() {
			return this.DO_FADING;
		}
	}

	class ActionTransitionDefinition {
		@FunctionalInterface public interface TransitionEvaluator {
			boolean shouldTransition(MarioTravelData data);
		}
		@FunctionalInterface public interface TransitionExecutorTravelling {
			void execute(MarioTravelData data);
		}
		@FunctionalInterface public interface TransitionExecutorClients {
			void execute(MarioClientSideData data, boolean isSelf, long seed);
		}

		public final Identifier TARGET_IDENTIFIER;
		public final TransitionEvaluator EVALUATOR;

		public final TransitionExecutorTravelling EXECUTOR_TRAVELLERS;
		public final TransitionExecutorClients EXECUTOR_CLIENTS;

		public ActionTransitionDefinition(
				@NotNull String targetID,
				@NotNull TransitionEvaluator evaluator,
				@Nullable TransitionExecutorTravelling executeTravel,
				@Nullable TransitionExecutorClients executeClients
		) {
			this.EVALUATOR = evaluator;
			this.TARGET_IDENTIFIER = Identifier.of(targetID);

			this.EXECUTOR_TRAVELLERS = executeTravel;
			this.EXECUTOR_CLIENTS = executeClients;
		}

		public ActionTransitionDefinition(
				@NotNull String targetID,
				@NotNull TransitionEvaluator evaluator
		) {
			this(targetID, evaluator, null, null);
		}

		public ActionTransitionDefinition(
				@NotNull String targetID,
				@NotNull TransitionEvaluator evaluator,
				@NotNull TransitionExecutorTravelling executeTravel
		) {
			this(targetID, evaluator, executeTravel, null);
		}
	}

	class ActionTransitionInjection {
		public final Identifier INJECT_NEAR_TRANSITIONS_TO;
		public final boolean INJECT_BEFORE_TARGET;
		public final ActionCategory ONLY_FOR_CATEGORY;
		public final ActionTransitionDefinition TRANSITION;

		public ActionTransitionInjection(
				InjectionPlacement placement,
				String injectNearTransitionsTo,
				ActionCategory category,
				ActionTransitionDefinition injectedTransition
		) {
			INJECT_NEAR_TRANSITIONS_TO = Identifier.of(injectNearTransitionsTo);
			INJECT_BEFORE_TARGET = placement == InjectionPlacement.BEFORE;
			ONLY_FOR_CATEGORY = category;
			TRANSITION = injectedTransition;
		}

		public enum InjectionPlacement {
			BEFORE,
			AFTER
		}

		public enum ActionCategory {
			ANY(true, true, true),
			GROUNDED(true, false, false),
			AIRBORNE(false, true, false),
			AQUATIC(false, false, true);

			public final boolean IS_GROUNDED;
			public final boolean IS_AIRBORNE;
			public final boolean IS_AQUATIC;

			ActionCategory(boolean grounded, boolean airborne, boolean aquatic) {
				this.IS_GROUNDED = grounded;
				this.IS_AIRBORNE = airborne;
				this.IS_AQUATIC = aquatic;
			}
		}
	}
}
