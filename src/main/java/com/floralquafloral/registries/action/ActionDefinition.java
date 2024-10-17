package com.floralquafloral.registries.action;

import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.MarioStateDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ActionDefinition extends MarioStateDefinition {
	@Nullable String getAnimationName();

	void selfTick(MarioClientData data);
	void otherClientsTick(MarioPlayerData data);
	void serverTick(MarioPlayerData data);

	List<ActionTransitionDefinition> getPreTickTransitions();
	List<ActionTransitionDefinition> getPostTickTransitions();
	List<ActionTransitionDefinition> getPostMoveTransitions();
	List<ActionTransitionInjection> getTransitionInjections();

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
			void execute(MarioPlayerData data, boolean isSelf);
		}
		@FunctionalInterface public interface TransitionExecutor {
			void execute(MarioPlayerData data);
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
