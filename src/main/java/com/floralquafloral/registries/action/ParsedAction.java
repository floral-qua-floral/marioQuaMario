package com.floralquafloral.registries.action;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioDataPackets;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ParsedAction {
	public final Identifier ID;
	private final ActionDefinition DEFINITION;
	public final String ANIMATION;

	private final EnumMap<TransitionPhase, List<ParsedTransition>> TRANSITION_LISTS;

	public ParsedAction(ActionDefinition definition) {
		this.ID = definition.getID();
		this.DEFINITION = definition;
		this.ANIMATION = definition.getAnimationName();

		this.TRANSITION_LISTS = new EnumMap<>(TransitionPhase.class);
	}

	public void attemptTransitions(MarioClientData data, TransitionPhase phase) {
		for(ParsedTransition transition : this.TRANSITION_LISTS.get(phase)) {
			if(transition.EVALUATOR.shouldTransition(data)) {
				// Send C2S packet to tell the server!

				transition.EXECUTOR_CLIENT.execute(data, true);
				MarioDataPackets.broadcastSetMarioAction(transition.TARGET_ACTION);
				data.setAction(transition.TARGET_ACTION);
				return;
			}
		}
	}

	public void transitionTo(MarioPlayerData data, ParsedAction toAction) {
		if(transitionTo(data, toAction, TransitionPhase.PRE_TICK)) return;
		if(transitionTo(data, toAction, TransitionPhase.POST_TICK)) return;
		transitionTo(data, toAction, TransitionPhase.POST_MOVE);
	}
	private boolean transitionTo(MarioPlayerData data, ParsedAction toAction, TransitionPhase checkInPhase) {
		for(ParsedTransition transition : this.TRANSITION_LISTS.get(checkInPhase)) {
			if(transition.TARGET_ACTION == toAction) {
				transition.execute(data);
				return true;
			}
		}
		return false;
	}

	public void selfTick(MarioClientData data) {
		this.DEFINITION.selfTick(data);
	}
	public void otherClientsTick(MarioPlayerData data) {
		this.DEFINITION.otherClientsTick(data);
	}
	public void serverTick(MarioPlayerData data) {
		this.DEFINITION.serverTick(data);
	}

	public void populateTransitionLists(Map<Identifier, ArrayList<ActionDefinition.ActionTransitionDefinition>> injections) {
		MarioQuaMario.LOGGER.info("Parsing transitions out of {}...", this.ID);
		MarioQuaMario.LOGGER.info("PRE-TICK:-------");
		this.TRANSITION_LISTS.put(TransitionPhase.PRE_TICK, parseTransitionDefinitions(this.DEFINITION.getPreTickTransitions(), injections));
		MarioQuaMario.LOGGER.info("POST-TICK:------");
		this.TRANSITION_LISTS.put(TransitionPhase.POST_TICK, parseTransitionDefinitions(this.DEFINITION.getPostTickTransitions(), injections));
		MarioQuaMario.LOGGER.info("POST-MOVE:------");
		this.TRANSITION_LISTS.put(TransitionPhase.POST_MOVE, parseTransitionDefinitions(this.DEFINITION.getPostMoveTransitions(), injections));
	}

	private static List<ParsedTransition> parseTransitionDefinitions(
			List<ActionDefinition.ActionTransitionDefinition> definitions,
			Map<Identifier, ArrayList<ActionDefinition.ActionTransitionDefinition>> injections
	) {
		List<ParsedTransition> workingList = new ArrayList<>();
		for(ActionDefinition.ActionTransitionDefinition definition : definitions) {
			MarioQuaMario.LOGGER.info("Parsing transition to {}", definition.TARGET_IDENTIFIER);
			if(injections.containsKey(definition.TARGET_IDENTIFIER)) {
				MarioQuaMario.LOGGER.info("Injections found that should be inserted before this transition! Injecting:");
				for(ActionDefinition.ActionTransitionDefinition injectTransition : injections.get(definition.TARGET_IDENTIFIER)) {
					MarioQuaMario.LOGGER.info("Parsing & injecting transition to {}", injectTransition.TARGET_IDENTIFIER);
					workingList.add(new ParsedTransition(injectTransition));
				}
				MarioQuaMario.LOGGER.info("Finished injections, now parsing {} as planned", definition.TARGET_IDENTIFIER);
			}
			workingList.add(new ParsedTransition(definition));
		}
		return workingList;
	}

	private static class ParsedTransition {
		private final ParsedAction TARGET_ACTION;

		private final ActionDefinition.ActionTransitionDefinition.TransitionEvaluator EVALUATOR;

		private final ActionDefinition.ActionTransitionDefinition.TransitionExecutorClient EXECUTOR_CLIENT;
		private final ActionDefinition.ActionTransitionDefinition.TransitionExecutor EXECUTOR_SERVER;

		private ParsedTransition(ActionDefinition.ActionTransitionDefinition definition) {
			TARGET_ACTION = RegistryManager.ACTIONS.get(definition.TARGET_IDENTIFIER);
			EVALUATOR = definition.EVALUATOR;
			EXECUTOR_CLIENT = definition.EXECUTOR_CLIENT;
			EXECUTOR_SERVER = definition.EXECUTOR_SERVER;
		}

		private void execute(MarioPlayerData data) {
			if(data.MARIO.getWorld().isClient)
				this.EXECUTOR_CLIENT.execute(data, false);
			else
				this.EXECUTOR_SERVER.execute(data);
		}
	}
}
