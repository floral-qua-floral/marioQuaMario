package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ParsedTransition(
		@NotNull AbstractParsedAction targetAction,
		@NotNull TransitionDefinition.Evaluator evaluator,
		boolean fullyNetworked,
		boolean serverChecked,
		@Nullable TransitionDefinition.TravelExecutor travelExecutor,
		@Nullable TransitionDefinition.ClientsExecutor clientsExecutor
) {
	public ParsedTransition(TransitionDefinition definition) {
		this(
				getTargetAction(definition),
				definition.evaluator(),
				definition.environment() != EvaluatorEnvironment.COMMON,
				definition.environment() == EvaluatorEnvironment.CLIENT_CHECKED,
				definition.travelExecutor(),
				definition.clientsExecutor()
		);
	}

	public static @NotNull AbstractParsedAction getTargetAction(TransitionDefinition definition) {
		AbstractParsedAction targetAction = RegistryManager.ACTIONS.get(definition.targetID());
		if(targetAction == null) throw new CrashException(new CrashReport(
				"Attempting to register a transition into action \"" + definition.targetID()
						+ "\", but that action isn't registered! Check your entrypoints!",
				new InvalidTargetActionException("Transition to " + definition.targetID())));
		if(targetAction.CATEGORY == ActionCategory.WALLBOUND && switch (definition.environment()) {
			case CLIENT_ONLY, CLIENT_CHECKED -> false;
			case SERVER_ONLY, COMMON -> true;
		}) throw new CrashException(new CrashReport(
				"Attempting to register a server-only or common-sided transition into a wallbound action. " +
						"Wallbound action transitions must ALWAYS be initiated by the client!!! Use CLIENT_CHECKED " +
						"instead!",
				new InvalidTargetActionException("Non-client-initiated transition to wallbound action (" + targetAction.ID + ")")));
		return targetAction;
	}

	public static class InvalidTargetActionException extends RuntimeException {
		public InvalidTargetActionException(String message) {
			super(message);
		}
	}
}
