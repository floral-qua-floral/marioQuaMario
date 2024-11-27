package com.floralquafloral.registries.states.action;

import com.floralquafloral.*;
import com.floralquafloral.definitions.MarioAttackInterceptingStateDefinition;
import com.floralquafloral.definitions.actions.ActionDefinition;
import com.floralquafloral.definitions.actions.AirborneActionDefinition;
import com.floralquafloral.definitions.actions.AquaticActionDefinition;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.mariodata.*;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.registries.states.ParsedMarioState;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.stomp.ParsedStomp;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSeed;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;

import java.util.*;

public class ParsedAction extends ParsedMarioState {
	public final String ANIMATION;
	public final ActionDefinition.CameraAnimationSet CAMERA_ANIMATIONS;

	private final ActionDefinition ACTION_DEFINITION;

	public final ActionDefinition.SneakLegalityRule SNEAK_LEGALITY;
	public final ActionDefinition.SlidingStatus SLIDING_STATUS;
	public final ParsedStomp STOMP;
	public final ActionDefinition.BumpingRule BUMPING_RULE;

	private final EnumMap<TransitionPhase, List<ParsedTransition>> TRANSITION_LISTS;
	public final List<MarioAttackInterceptingStateDefinition.AttackInterceptionDefinition> INTERCEPTIONS;

	public ParsedAction(ActionDefinition definition) {
		super(definition);
		this.ACTION_DEFINITION = definition;
		this.ANIMATION = definition.getAnimationName();
		this.CAMERA_ANIMATIONS = definition.getCameraAnimations();
		this.SNEAK_LEGALITY = definition.getSneakLegalityRule();
		ActionDefinition.SlidingStatus status = definition.getActionSlidingStatus();
		this.SLIDING_STATUS = status == null ? ActionDefinition.SlidingStatus.NOT_SLIDING : status;
		Identifier stompID = definition.getStompType();
		this.STOMP = stompID == null ? null : RegistryManager.STOMP_TYPES.get(stompID);
		this.BUMPING_RULE = definition.getBumpingRule();

		this.TRANSITION_LISTS = new EnumMap<>(TransitionPhase.class);
		this.INTERCEPTIONS = definition.getUnarmedAttackInterceptions();
	}

	public @Nullable ActionDefinition.CameraAnimation getCameraAnimation() {
		if(this.CAMERA_ANIMATIONS == null) return null;
		return this.CAMERA_ANIMATIONS.AUTHENTIC_ANIMATION;
	}

	public void travelHook(MarioTravelData data) {
		this.ACTION_DEFINITION.travelHook(data);
	}

	public boolean attemptTransitions(MarioMainClientData data, TransitionPhase phase) {
		for(ParsedTransition transition : this.TRANSITION_LISTS.get(phase)) {
			if(transition.EVALUATOR.shouldTransition(data)) {
				long seed = RandomSeed.getSeed();
				if(transition.EXECUTOR_TRAVELLERS != null) transition.EXECUTOR_TRAVELLERS.execute(data);
				if(transition.EXECUTOR_CLIENTS != null) transition.EXECUTOR_CLIENTS.execute(data, true, seed);
				MarioDataPackets.broadcastSetMarioAction(transition.TARGET_ACTION, seed);
				data.setActionTransitionless(transition.TARGET_ACTION);
				return true;
			}
		}
		return false;
	}

	public boolean transitionTo(MarioPlayerData data, ParsedAction toAction, long seed) {
		if(
				transitionTo(data, toAction, TransitionPhase.PRE_TRAVEL, seed) ||
				transitionTo(data, toAction, TransitionPhase.INPUT, seed) ||
				transitionTo(data, toAction, TransitionPhase.WORLD_COLLISION, seed)
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

	public void populateTransitionLists(Map<Identifier, ArrayList<ActionDefinition.ActionTransitionInjection>> injections) {
		MarioQuaMario.LOGGER.info("Parsing transitions out of {}...", this.ID);
		ActionDefinition definition = (ActionDefinition) this.DEFINITION;
		MarioQuaMario.LOGGER.info("PRE-TICK:-------");
		this.TRANSITION_LISTS.put(TransitionPhase.PRE_TRAVEL, this.parseTransitionDefinitions(definition.getPreTravelTransitions(), injections));
		MarioQuaMario.LOGGER.info("POST-TICK:------");
		this.TRANSITION_LISTS.put(TransitionPhase.INPUT, this.parseTransitionDefinitions(definition.getInputTransitions(), injections));
		MarioQuaMario.LOGGER.info("POST-MOVE:------");
		this.TRANSITION_LISTS.put(TransitionPhase.WORLD_COLLISION, this.parseTransitionDefinitions(definition.getWorldCollisionTransitions(), injections));
	}

	private List<ParsedTransition> parseTransitionDefinitions(
			List<ActionDefinition.ActionTransitionDefinition> definitions,
			Map<Identifier, ArrayList<ActionDefinition.ActionTransitionInjection>> injections
	) {
		List<ParsedTransition> workingList = new ArrayList<>();
		for(ActionDefinition.ActionTransitionDefinition definition : definitions) {
			MarioQuaMario.LOGGER.info("Parsing transition to {}", definition.TARGET_IDENTIFIER);

			ArrayList<ActionDefinition.ActionTransitionInjection> relevantInjections = injections.get(definition.TARGET_IDENTIFIER);
			handleInjections(workingList, relevantInjections, true);
			workingList.add(new ParsedTransition(definition));
			handleInjections(workingList, relevantInjections, false);
		}
		return workingList;
	}

	private void handleInjections(
			List<ParsedTransition> workingList,
			@Nullable ArrayList<ActionDefinition.ActionTransitionInjection> relevantInjections,
			boolean isBefore
	) {
		if(relevantInjections == null) return;

		MarioQuaMario.LOGGER.info("Some injections exist for this transition! Inserting those for placement {}...", (isBefore ? "BEFORE": "AFTER"));

		for(ActionDefinition.ActionTransitionInjection injection : relevantInjections) {
			boolean timingAligned = injection.INJECT_BEFORE_TARGET == isBefore;
			boolean categoryAligned = (
					injection.ONLY_FOR_CATEGORY == ActionDefinition.ActionTransitionInjection.ActionCategory.ANY
					|| (this.DEFINITION instanceof GroundedActionDefinition && injection.ONLY_FOR_CATEGORY.IS_GROUNDED)
					|| (this.DEFINITION instanceof AirborneActionDefinition && injection.ONLY_FOR_CATEGORY.IS_AIRBORNE)
					|| (this.DEFINITION instanceof AquaticActionDefinition && injection.ONLY_FOR_CATEGORY.IS_AQUATIC)
			);


			if(timingAligned && categoryAligned) {
				MarioQuaMario.LOGGER.info("Parsing & injecting transition to {}...", injection.TRANSITION.TARGET_IDENTIFIER);
				workingList.add(new ParsedTransition(injection.TRANSITION));
			}
			else {
				MarioQuaMario.LOGGER.info("Skipping injection to {}. Reason: {}", injection.TRANSITION.TARGET_IDENTIFIER,
						timingAligned ? "Should only occur for category " + injection.ONLY_FOR_CATEGORY
								: "Should be injected " + (injection.INJECT_BEFORE_TARGET ? "BEFORE": "AFTER") + " transition.");
			}
		}
	}

	private static class ParsedTransition {
		private final ParsedAction TARGET_ACTION;

		private final ActionDefinition.ActionTransitionDefinition.TransitionEvaluator EVALUATOR;

		private final ActionDefinition.ActionTransitionDefinition.TransitionExecutorTravelling EXECUTOR_TRAVELLERS;
		private final ActionDefinition.ActionTransitionDefinition.TransitionExecutorClients EXECUTOR_CLIENTS;

		private ParsedTransition(ActionDefinition.ActionTransitionDefinition definition) {
			TARGET_ACTION = Objects.requireNonNull(RegistryManager.ACTIONS.get(definition.TARGET_IDENTIFIER));
//			if(TARGET_ACTION == null) MarioQuaMario.LOGGER.error("Transition target isn't registered?!?! {}", definition.TARGET_IDENTIFIER);
			EVALUATOR = definition.EVALUATOR;
			EXECUTOR_TRAVELLERS = definition.EXECUTOR_TRAVELLERS;
			EXECUTOR_CLIENTS = definition.EXECUTOR_CLIENTS;
		}

		private void execute(MarioData data, long seed) {
			if(this.EXECUTOR_TRAVELLERS != null && data instanceof MarioTravelData travelData)
				this.EXECUTOR_TRAVELLERS.execute(travelData);

			if(this.EXECUTOR_CLIENTS != null && data instanceof MarioClientSideData clientData)
				this.EXECUTOR_CLIENTS.execute(clientData, data.getMario().isMainPlayer(), seed);
		}
	}
}
