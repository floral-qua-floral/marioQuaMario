package com.fqf.charaformact_api.definitions.states.actions.util;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param targetID The ID of the action that this transition leads to.
 * @param evaluator The Evaluator that determines if this transition should fire.
 * @param travelExecutor The effect the transition has on the character's motion.
 * @param clientsExecutor Client-side effects of this transition firing.
 */
public record TransitionDefinition(
		@NotNull Identifier targetID,
		@NotNull Evaluator evaluator, @NotNull EvaluatorEnvironment environment,
		@Nullable TravelExecutor travelExecutor,
		@Nullable ClientsExecutor clientsExecutor
) {
	/**
	 * Alternate constructor provided for convenience
	 */
	public TransitionDefinition(@NotNull Identifier targetID, @NotNull Evaluator evaluator, @NotNull EvaluatorEnvironment environment) {
		this(targetID, evaluator, environment, null, null);
	}

	public TransitionDefinition variate(
			@Nullable Identifier targetID,
			@Nullable Evaluator evaluator,
			@Nullable EvaluatorEnvironment environment,
			@Nullable TravelExecutor travelExecutor,
			@Nullable ClientsExecutor clientsExecutor
	) {
		return new TransitionDefinition(
				targetID == null ? this.targetID : targetID,
				evaluator == null ? this.evaluator : evaluator,
				environment == null ? this.environment : environment,
				travelExecutor == null ? this.travelExecutor : travelExecutor,
				clientsExecutor == null ? this.clientsExecutor : clientsExecutor
		);
	}
	public TransitionDefinition variate(@Nullable Identifier targetID, @Nullable Evaluator evaluator) {
		return this.variate(targetID, evaluator, null, null, null);
	}

	/**
	 * Runs on the client-side to test if the associated transition should occur.
	 */
	@FunctionalInterface public interface Evaluator {
		boolean shouldTransition(CfaReadableMotionData data);
	}

	/**
	 * Runs on the main client and on the server when the associated transition occurs.
	 * In a multiplayer environment, this won't run on your client when another player does the transition, but it will
	 * when you're the one transitioning.
	 */
	@FunctionalInterface public interface TravelExecutor {
		void execute(CfaTravelData data);
	}

	/**
	 * Runs on the client side for anyone who is in range to see the transition (not just the person doing it).
	 */
	@FunctionalInterface public interface ClientsExecutor {
		void execute(CfaClientData data, boolean isSelf, long seed);
	}
}
