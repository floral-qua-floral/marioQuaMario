package com.floralquafloral.registries.states.action;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioDataPackets;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.ParsedMarioState;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.stomp.ParsedStomp;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSeed;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ParsedAction extends ParsedMarioState {
	public final String ANIMATION;

	public final ActionDefinition.SneakLegalityRule SNEAK_LEGALITY;
	public final ActionDefinition.SlidingStatus SLIDING_STATUS;
	public final ParsedStomp STOMP;

	private final EnumMap<TransitionPhase, List<ParsedTransition>> TRANSITION_LISTS;

	public ParsedAction(ActionDefinition definition) {
		super(definition);
//		this.DEFINITION = definition;
		this.ANIMATION = definition.getAnimationName();
		this.SNEAK_LEGALITY = definition.getSneakLegalityRule();
		this.SLIDING_STATUS = definition.getConstantSlidingStatus();
		Identifier stompID = definition.getStompType();
		this.STOMP = stompID == null ? null : RegistryManager.STOMP_TYPES.get(stompID);

		this.TRANSITION_LISTS = new EnumMap<>(TransitionPhase.class);
	}

	public boolean attemptTransitions(MarioClientData data, TransitionPhase phase) {
		for(ParsedTransition transition : this.TRANSITION_LISTS.get(phase)) {
			if(transition.EVALUATOR.shouldTransition(data)) {
				// Send C2S packet to tell the server!

				long seed = RandomSeed.getSeed();
				if(transition.EXECUTOR_CLIENT != null) transition.EXECUTOR_CLIENT.execute(data, true, seed);
				MarioDataPackets.broadcastSetMarioAction(transition.TARGET_ACTION, seed);
				data.setAction(transition.TARGET_ACTION, seed);
				return true;
			}
		}
		return false;
	}

	public boolean transitionTo(MarioPlayerData data, ParsedAction toAction, long seed) {
		if(
				transitionTo(data, toAction, TransitionPhase.PRE_TICK, seed) ||
				transitionTo(data, toAction, TransitionPhase.POST_TICK, seed) ||
				transitionTo(data, toAction, TransitionPhase.POST_MOVE, seed)
		) return true;

		MarioQuaMario.LOGGER.warn("{} attempted an invalid action transition: {} -> {}", data.getMario().getName().getString(), this.ID, toAction.ID);
		return false;
	}
	private boolean transitionTo(MarioPlayerData data, ParsedAction toAction, TransitionPhase checkInPhase, long seed) {
		for(ParsedTransition transition : this.TRANSITION_LISTS.get(checkInPhase)) {
			if(transition.TARGET_ACTION == toAction) {
				transition.execute(data, seed);
				return true;
			}
		}
		return false;
	}

	public void populateTransitionLists(Map<Identifier, ArrayList<ActionDefinition.ActionTransitionDefinition>> injections) {
		MarioQuaMario.LOGGER.info("Parsing transitions out of {}...", this.ID);
		ActionDefinition definition = (ActionDefinition) this.DEFINITION;
		MarioQuaMario.LOGGER.info("PRE-TICK:-------");
		this.TRANSITION_LISTS.put(TransitionPhase.PRE_TICK, parseTransitionDefinitions(definition.getPreTickTransitions(), injections));
		MarioQuaMario.LOGGER.info("POST-TICK:------");
		this.TRANSITION_LISTS.put(TransitionPhase.POST_TICK, parseTransitionDefinitions(definition.getPostTickTransitions(), injections));
		MarioQuaMario.LOGGER.info("POST-MOVE:------");
		this.TRANSITION_LISTS.put(TransitionPhase.POST_MOVE, parseTransitionDefinitions(definition.getPostMoveTransitions(), injections));
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
			if(TARGET_ACTION == null) MarioQuaMario.LOGGER.error("Transition target isn't registered?!?! {}", definition.TARGET_IDENTIFIER);
			EVALUATOR = definition.EVALUATOR;
			EXECUTOR_CLIENT = definition.EXECUTOR_CLIENT;
			EXECUTOR_SERVER = definition.EXECUTOR_SERVER;
		}

		private void execute(MarioPlayerData data, long seed) {
			if(data.getMario().getWorld().isClient) {
				if (this.EXECUTOR_CLIENT != null)
					this.EXECUTOR_CLIENT.execute(data, false, seed);
			}
			else if (this.EXECUTOR_SERVER != null) this.EXECUTOR_SERVER.execute(data, seed);
		}
	}
}
