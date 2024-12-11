package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.actions.ActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParsedAction {
	private final ActionDefinition DEFINITION;

	public final Identifier ID;

	public final @Nullable String ANIMATION;
	public final @Nullable CameraAnimationSet CAMERA_ANIMATIONS;
	public final SlidingStatus SLIDING_STATUS;

	public final SneakingRule SNEAKING_RULE;
	public final SprintingRule SPRINTING_RULE;

	public final @Nullable BumpType BUMP_TYPE;
//	public final @Nullable ParsedStompType STOMP_TYPE;

	public final EnumMap<TransitionPhase, List<ParsedTransition>> TRANSITIONS;

	public ParsedAction(ActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		this.DEFINITION = definition;

		this.ID = definition.getID();
		this.ANIMATION = definition.getAnimationName();
		this.CAMERA_ANIMATIONS = definition.getCameraAnimations();
		this.SLIDING_STATUS = definition.getSlidingStatus();

		this.SNEAKING_RULE = definition.getSneakingRule();
		this.SPRINTING_RULE = definition.getSprintingRule();

		this.BUMP_TYPE = definition.getBumpType();

		this.TRANSITIONS = new EnumMap<>(TransitionPhase.class);

		for(TransitionInjectionDefinition injection : definition.getTransitionInjections()) {
			allInjections.putIfAbsent(injection.injectNearTransitionsTo(), new HashSet<>());
			allInjections.get(injection.injectNearTransitionsTo()).add(injection);
		}
	}

	public void parseTransitions(HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		this.parseTransitions(TransitionPhase.BASIC, this.DEFINITION.getBasicTransitions(), allInjections);
		this.parseTransitions(TransitionPhase.INPUT, this.DEFINITION.getInputTransitions(), allInjections);
		this.parseTransitions(TransitionPhase.WORLD_COLLISION, this.DEFINITION.getWorldCollisionTransitions(), allInjections);
	}
	private void parseTransitions(
			TransitionPhase phase, List<TransitionDefinition> transitions,
			HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections
	) {
		this.TRANSITIONS.putIfAbsent(phase, new ArrayList<>());
		List<ParsedTransition> buildingTransitionList = this.TRANSITIONS.get(phase);

		for(TransitionDefinition definition : transitions) {
			Set<TransitionInjectionDefinition> relevantInjections = new HashSet<>(allInjections.get(definition.targetID()));
			relevantInjections.removeIf(injection -> !this.isOfCategory(injection.category()));

			this.conditionallyInjectTransitions(buildingTransitionList, relevantInjections,
					TransitionInjectionDefinition.InjectionPlacement.BEFORE, definition);
			buildingTransitionList.add(new ParsedTransition(definition));
		}
	}
	private void conditionallyInjectTransitions(
			List<ParsedTransition> buildingTransitionList,
			Set<TransitionInjectionDefinition> relevantInjections,
			TransitionInjectionDefinition.InjectionPlacement placement,
			TransitionDefinition originalTransition
	) {
		for(TransitionInjectionDefinition injection : relevantInjections) {
			if(injection.placement() == placement) {
				buildingTransitionList.add(new ParsedTransition(injection.injectedTransitionCreator().makeTransition(originalTransition)));
			}
		}
	}

	private boolean isOfCategory(TransitionInjectionDefinition.ActionCategory category) {
		return true;
	}
}
