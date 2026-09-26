package com.fqf.charaformact_api.definitions.states.actions.util;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @param targetID           The ID of the action that this transition leads to.
 * @param evaluator          The Evaluator that determines if this transition should fire.
 * @param sizeChangeBehavior
 * @param travelExecutor     The effect the transition has on the character's motion.
 * @param clientsExecutor    Client-side effects of this transition firing.
 */
public record ActionTransitionDetails(
		@NotNull Identifier targetID,
		@NotNull Predicate<CfaReadableMotionData> evaluator, @NotNull EvaluatorEnvironment environment,
		SizeChangeBehavior sizeChangeBehavior,
		@Nullable TravelExecutor travelExecutor,
		@Nullable ClientsExecutor clientsExecutor
) {
	/**
	 * Alternate constructors provided for convenience
	 */
	public ActionTransitionDetails(
			@NotNull Identifier targetID,
			@NotNull Predicate<CfaReadableMotionData> evaluator, @NotNull EvaluatorEnvironment environment,
			@Nullable TravelExecutor travelExecutor,
			@Nullable ClientsExecutor clientsExecutor
	) {
		this(targetID, evaluator, environment, SizeChangeBehavior.AUTOMATIC, travelExecutor, clientsExecutor);
	}
	public ActionTransitionDetails(
			@NotNull Identifier targetID,
			@NotNull Predicate<CfaReadableMotionData> evaluator, @NotNull EvaluatorEnvironment environment,
			@NotNull SizeChangeBehavior sizeChangeBehavior
	) {
		this(targetID, evaluator, environment, sizeChangeBehavior, null, null);
	}
	public ActionTransitionDetails(
			@NotNull Identifier targetID,
			@NotNull Predicate<CfaReadableMotionData> evaluator, @NotNull EvaluatorEnvironment environment
	) {
		this(targetID, evaluator, environment, SizeChangeBehavior.AUTOMATIC, null, null);
	}

	/**
	 * Call on an existing ActionTransitionDetails to make a copy with certain components modified.
	 */
	public ActionTransitionDetails variate(
			@Nullable Identifier targetID,
			@Nullable Predicate<CfaReadableMotionData> evaluator, @Nullable EvaluatorEnvironment environment,
			@Nullable SizeChangeBehavior sizeChangeBehavior,
			@Nullable TravelExecutor travelExecutor,
			@Nullable ClientsExecutor clientsExecutor
	) {
		return new ActionTransitionDetails(
				targetID == null ? this.targetID : targetID,
				evaluator == null ? this.evaluator : evaluator, environment == null ? this.environment : environment,
				sizeChangeBehavior == null ? this.sizeChangeBehavior : sizeChangeBehavior,
				travelExecutor == null ? this.travelExecutor : travelExecutor,
				clientsExecutor == null ? this.clientsExecutor : clientsExecutor
		);
	}
	public ActionTransitionDetails variate(@Nullable Identifier targetID, @Nullable Predicate<CfaReadableMotionData> evaluator) {
		return this.variate(targetID, evaluator, null, null, null, null);
	}
	public ActionTransitionDetails applyVariator(Function<ActionTransitionDetails, ActionTransitionDetails> variator) {
		return variator.apply(this);
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
