package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.CameraAnimationSet;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.ParsedAttackInterception;
import com.fqf.mario_qua_mario.registries.ParsedMarioState;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractParsedAction extends ParsedMarioState {
	protected final IncompleteActionDefinition ACTION_DEFINITION;

	public final @Nullable PlayermodelAnimation ANIMATION;
	public final @Nullable CameraAnimationSet CAMERA_ANIMATIONS;
	public final SlidingStatus SLIDING_STATUS;

	public final SneakingRule SNEAKING_RULE;
	public final SprintingRule SPRINTING_RULE;

	public final @Nullable BumpType BUMP_TYPE;
//	public final @Nullable ParsedStompType STOMP_TYPE;

	public final Map<AbstractParsedAction, ParsedTransition> TRANSITIONS_FROM_TARGETS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> CLIENT_TRANSITIONS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> SERVER_TRANSITIONS;

	public final List<ParsedAttackInterception> INTERCEPTIONS;

	public AbstractParsedAction(IncompleteActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition);
		this.ACTION_DEFINITION = definition;

		this.ANIMATION = definition.getAnimation(null);
		this.CAMERA_ANIMATIONS = definition.getCameraAnimations();
		this.SLIDING_STATUS = definition.getSlidingStatus();

		this.SNEAKING_RULE = definition.getSneakingRule();
		this.SPRINTING_RULE = definition.getSprintingRule();

		this.BUMP_TYPE = definition.getBumpType();

		this.TRANSITIONS_FROM_TARGETS = new HashMap<>();
		this.CLIENT_TRANSITIONS = new EnumMap<>(TransitionPhase.class);
		this.SERVER_TRANSITIONS = new EnumMap<>(TransitionPhase.class);

		for(TransitionInjectionDefinition injection : definition.getTransitionInjections()) {
			allInjections.putIfAbsent(injection.injectNearTransitionsTo(), new HashSet<>());
			allInjections.get(injection.injectNearTransitionsTo()).add(injection);
		}

		this.INTERCEPTIONS = new ArrayList<>();
	}

	protected abstract List<TransitionDefinition> getBasicTransitions();
	protected abstract List<TransitionDefinition> getInputTransitions();
	protected abstract List<TransitionDefinition> getWorldCollisionTransitions();

	public void parseTransitions(HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		this.parseTransitions(TransitionPhase.BASIC, this.getBasicTransitions(), allInjections);
		this.parseTransitions(TransitionPhase.INPUT, this.getInputTransitions(), allInjections);
		this.parseTransitions(TransitionPhase.WORLD_COLLISION, this.getWorldCollisionTransitions(), allInjections);

		for (AttackInterceptingStateDefinition.AttackInterceptionDefinition interception : this.ACTION_DEFINITION.getAttackInterceptions()) {
			this.INTERCEPTIONS.add(new ParsedAttackInterception(interception, true));
		}
	}
	private void parseTransitions(
			TransitionPhase phase, List<TransitionDefinition> transitions,
			HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections
	) {
		this.CLIENT_TRANSITIONS.putIfAbsent(phase, new ArrayList<>());
		this.SERVER_TRANSITIONS.putIfAbsent(phase, new ArrayList<>());
		List<ParsedTransition> buildingClientList = this.CLIENT_TRANSITIONS.get(phase);
		List<ParsedTransition> buildingServerList = this.SERVER_TRANSITIONS.get(phase);

		for(TransitionDefinition definition : transitions) {
			Set<TransitionInjectionDefinition> relevantInjections = new HashSet<>(allInjections.getOrDefault(definition.targetID(), Set.of()));

			relevantInjections.removeIf(injection -> injection.category() != TransitionInjectionDefinition.ActionCategory.ANY && injection.category() != this.getCategory());

			MarioQuaMario.LOGGER.info("TRANSITION INJECTIONS RELEVANT TO {}:", definition.targetID());
			for (TransitionInjectionDefinition relevantInjection : relevantInjections) {
				MarioQuaMario.LOGGER.info("This one: {}", relevantInjection);
			}

			this.conditionallyInjectTransitions(buildingClientList, buildingServerList, relevantInjections,
					TransitionInjectionDefinition.InjectionPlacement.BEFORE, definition);
			MarioQuaMario.LOGGER.info("Parsing transition into {}", definition.targetID());
			addTransitionToLists(buildingClientList, buildingServerList, definition);
			this.conditionallyInjectTransitions(buildingClientList, buildingServerList, relevantInjections,
					TransitionInjectionDefinition.InjectionPlacement.AFTER, definition);
		}
	}
	private void conditionallyInjectTransitions(
			List<ParsedTransition> buildingClientList,
			List<ParsedTransition> buildingServerList,
			Set<TransitionInjectionDefinition> relevantInjections,
			TransitionInjectionDefinition.InjectionPlacement placement,
			TransitionDefinition originalTransition
	) {
		for(TransitionInjectionDefinition injection : relevantInjections) {
			if(injection.placement() == placement) {
				addTransitionToLists(buildingClientList, buildingServerList, injection.injectedTransitionCreator().makeTransition(originalTransition));
			}
		}
	}

	private void addTransitionToLists(
			List<ParsedTransition> client, List<ParsedTransition> server,
			TransitionDefinition definition
	) {
		ParsedTransition transition = new ParsedTransition(definition);
		if(this.TRANSITIONS_FROM_TARGETS.containsKey(transition.targetAction()))
			MarioQuaMario.LOGGER.warn("Action {} has multiple transitions into {}! This is likely to cause issues!",
					this.ID, transition.targetAction().ID);
		else this.TRANSITIONS_FROM_TARGETS.put(transition.targetAction(), transition);
		if(definition.environment() != EvaluatorEnvironment.SERVER_ONLY) client.add(transition);
		if(definition.environment() != EvaluatorEnvironment.CLIENT_ONLY && definition.environment() != EvaluatorEnvironment.CLIENT_CHECKED)
			server.add(transition);
	}

	public int getIntID() {
		return RegistryManager.ACTIONS.getRawIdOrThrow(this);
	}

	abstract public boolean travelHook(MarioMoveableData data);

	abstract protected TransitionInjectionDefinition.ActionCategory getCategory();
}
