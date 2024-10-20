package com.floralquafloral.registries.states.action;

import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.states.MarioStateDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ActionDefinition extends MarioStateDefinition {
	@Nullable String getAnimationName();
	SneakLegalityRule getSneakLegalityRule();
	SlidingStatus getConstantSlidingStatus();
	@Nullable Identifier getStompType();

	List<ActionTransitionDefinition> getPreTickTransitions();
	List<ActionTransitionDefinition> getPostTickTransitions();
	List<ActionTransitionDefinition> getPostMoveTransitions();
	List<ActionTransitionInjection> getTransitionInjections();

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
		SLIDING(false, false, true, true, false),				// No view bobbing or footsteps, with sliding sound & block particles
		SLIDING_NO_PARTICLES(false, false, true, false, false),	// No view bobbing or footsteps, with sliding sound
		WALL_SLIDING(false, false, false, false, true),			// No view bobbing or footsteps, with alternate sliding sound
		SLIDING_SILENT(false, false, false, false, false),		// No view bobbing or footsteps.
		NOT_SLIDING_SMOOTH(true, false, false, false, false),	// Footsteps, but no view bobbing
		NOT_SLIDING(true, true, false, false, false);			// Vanilla view bobbing & footstep sounds

		private final boolean DO_FOOTSTEPS;
		private final boolean DO_VIEW_BOBBING;
		private final boolean DO_SLIDE_SFX;
		private final boolean DO_ALT_SLIDE_SFX;
		private final boolean DO_SLIDE_PARTICLES;

		SlidingStatus(boolean doFootsteps, boolean doViewBobbing, boolean doSfx, boolean doParticles, boolean isWall) {
			this.DO_FOOTSTEPS = doFootsteps;
			this.DO_VIEW_BOBBING = doViewBobbing;
			this.DO_SLIDE_SFX = doSfx;
			this.DO_SLIDE_PARTICLES = doParticles;
			this.DO_ALT_SLIDE_SFX = isWall;
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
	}

	class ActionTransitionInjection {
		public final Identifier INJECT_BEFORE_TRANSITIONS_TO;
		public final ActionTransitionDefinition TRANSITION;

		public ActionTransitionInjection(
				String injectBeforeTransitionsTo,
				ActionTransitionDefinition injectedTransition
		) {
			INJECT_BEFORE_TRANSITIONS_TO = Identifier.of(injectBeforeTransitionsTo);
			TRANSITION = injectedTransition;
		}
	}

	class ActionTransitionDefinition {
		@FunctionalInterface public interface TransitionEvaluator {
			boolean shouldTransition(MarioClientData data);
		}
		@FunctionalInterface public interface TransitionExecutorClient {
			void execute(MarioPlayerData data, boolean isSelf, long seed);
		}
		@FunctionalInterface public interface TransitionExecutor {
			void execute(MarioPlayerData data, long seed);
		}

		public final Identifier TARGET_IDENTIFIER;
		public final TransitionEvaluator EVALUATOR;
		public final TransitionExecutorClient EXECUTOR_CLIENT;
		public final TransitionExecutor EXECUTOR_SERVER;

		public ActionTransitionDefinition(
				@NotNull String targetID,
				@NotNull TransitionEvaluator evaluator,
				@Nullable TransitionExecutorClient executeClient,
				@Nullable TransitionExecutor executeServer
		) {
			this.EVALUATOR = evaluator;
			this.TARGET_IDENTIFIER = Identifier.of(targetID);
			this.EXECUTOR_CLIENT = executeClient;
			this.EXECUTOR_SERVER = executeServer;
		}

		public ActionTransitionDefinition(
				@NotNull String targetID,
				@NotNull TransitionEvaluator evaluator
		) {
			this(targetID, evaluator, null, null);
		}
	}
}
