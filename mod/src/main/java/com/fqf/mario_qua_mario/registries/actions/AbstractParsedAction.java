package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.*;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.ParsedMarioThing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractParsedAction extends ParsedMarioThing {
	protected final IncompleteActionDefinition DEFINITION;

	public final Identifier ID;

	public final @Nullable String ANIMATION;
	public final @Nullable CameraAnimationSet CAMERA_ANIMATIONS;
	public final SlidingStatus SLIDING_STATUS;

	public final SneakingRule SNEAKING_RULE;
	public final SprintingRule SPRINTING_RULE;

	public final @Nullable BumpType BUMP_TYPE;
//	public final @Nullable ParsedStompType STOMP_TYPE;

	public final List<ParsedTransition> ALL_TRANSITIONS;

	public final Map<AbstractParsedAction, ParsedTransition> TRANSITIONS_FROM_TARGETS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> CLIENT_TRANSITIONS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> SERVER_TRANSITIONS;

	public AbstractParsedAction(IncompleteActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition);
		this.DEFINITION = definition;

		this.ID = definition.getID();
		this.ANIMATION = definition.getAnimationName();
		this.CAMERA_ANIMATIONS = definition.getCameraAnimations();
		this.SLIDING_STATUS = definition.getSlidingStatus();

		this.SNEAKING_RULE = definition.getSneakingRule();
		this.SPRINTING_RULE = definition.getSprintingRule();

		this.BUMP_TYPE = definition.getBumpType();

		this.ALL_TRANSITIONS = new ArrayList<>();
		this.TRANSITIONS_FROM_TARGETS = new HashMap<>();
		this.CLIENT_TRANSITIONS = new EnumMap<>(TransitionPhase.class);
		this.SERVER_TRANSITIONS = new EnumMap<>(TransitionPhase.class);

		for(TransitionInjectionDefinition injection : definition.getTransitionInjections()) {
			allInjections.putIfAbsent(injection.injectNearTransitionsTo(), new HashSet<>());
			allInjections.get(injection.injectNearTransitionsTo()).add(injection);
		}
	}

	protected abstract List<TransitionDefinition> getBasicTransitions();
	protected abstract List<TransitionDefinition> getInputTransitions();
	protected abstract List<TransitionDefinition> getWorldCollisionTransitions();

	public void parseTransitions(HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		this.parseTransitions(TransitionPhase.BASIC, this.getBasicTransitions(), allInjections);
		this.parseTransitions(TransitionPhase.INPUT, this.getInputTransitions(), allInjections);
		this.parseTransitions(TransitionPhase.WORLD_COLLISION, this.getWorldCollisionTransitions(), allInjections);
	}
	private void parseTransitions(
			TransitionPhase phase, List<TransitionDefinition> transitions,
			HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections
	) {
		this.CLIENT_TRANSITIONS.putIfAbsent(phase, new ArrayList<>());
		List<ParsedTransition> buildingClientList = this.CLIENT_TRANSITIONS.get(phase);
		List<ParsedTransition> buildingServerList = this.SERVER_TRANSITIONS.get(phase);

		for(TransitionDefinition definition : transitions) {
			Set<TransitionInjectionDefinition> relevantInjections = new HashSet<>(allInjections.getOrDefault(definition.targetID(), Set.of()));
			relevantInjections.removeIf(injection -> injection.category() != TransitionInjectionDefinition.ActionCategory.ANY && injection.category() != this.getCategory());

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
		this.ALL_TRANSITIONS.add(transition);
		if(this.TRANSITIONS_FROM_TARGETS.containsKey(transition.targetAction()))
			MarioQuaMario.LOGGER.warn("Action {} has multiple transitions into {}! This is likely to cause issues!",
					this.ID, transition.targetAction().ID);
		else this.TRANSITIONS_FROM_TARGETS.put(transition.targetAction(), transition);
		if(definition.context() != EvaluatorContext.CLIENT_ONLY) server.add(transition);
		if(definition.context() != EvaluatorContext.SERVER_ONLY) client.add(transition);
	}

	abstract public void travelHook(MarioMoveableData data);

	abstract protected TransitionInjectionDefinition.ActionCategory getCategory();
}
