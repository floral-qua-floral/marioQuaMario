package com.fqf.charapoweract.registries.actions;

import com.fqf.charapoweract.MarioQuaMario;
import com.fqf.charapoweract.registries.ParsedCollisionAttackType;
import com.fqf.charapoweract_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.*;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.charapoweract.mariodata.MarioMoveableData;
import com.fqf.charapoweract.registries.ParsedAttackInterception;
import com.fqf.charapoweract.registries.ParsedMarioState;
import com.fqf.charapoweract.registries.RegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractParsedAction extends ParsedMarioState {
	protected final IncompleteActionDefinition ACTION_DEFINITION;
	public final ActionCategory CATEGORY;

	public final @Nullable PlayermodelAnimation ANIMATION;
	public final @Nullable CameraAnimationSet CAMERA_ANIMATIONS;
	public final SlidingStatus SLIDING_STATUS;

	public final SneakingRule SNEAKING_RULE;
	public final SprintingRule SPRINTING_RULE;
	public final @Nullable GenericActionType GENERIC_ACTION_TYPE;

	public final @NotNull BappingRule BAPPING_RULE;
	public final @Nullable ParsedCollisionAttackType COLLISION_ATTACK_TYPE;

	public final Map<AbstractParsedAction, ParsedTransition> TRANSITIONS_FROM_TARGETS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> CLIENT_TRANSITIONS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> SERVER_TRANSITIONS;

	public final List<ParsedAttackInterception> INTERCEPTIONS;

	private static final boolean LOG_TRANSITION_INJECTIONS = MarioQuaMario.CONFIG.logActionTransitionInjections();

	private static final BappingRule NULL_EQUIVALENT = new BappingRule(0, 0);

	public AbstractParsedAction(IncompleteActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition);

		MarioQuaMario.LOGGER.info("Parsing action {}...", this.ID);

		this.ACTION_DEFINITION = definition;
		this.CATEGORY = this.getCategory();

		this.ANIMATION = definition.getAnimation(AnimationHelperImpl.INSTANCE);
		this.CAMERA_ANIMATIONS = definition.getCameraAnimations(AnimationHelperImpl.INSTANCE);
		this.SLIDING_STATUS = definition.getSlidingStatus();

		this.SNEAKING_RULE = definition.getSneakingRule();
		this.SPRINTING_RULE = definition.getSprintingRule();
		this.GENERIC_ACTION_TYPE = (definition instanceof GenericActionDefinition genericDefinition ? genericDefinition.getGenericActionType() : null);

		BappingRule bappingRule = definition.getBappingRule();
		this.BAPPING_RULE = bappingRule == null ? NULL_EQUIVALENT : bappingRule;
		this.COLLISION_ATTACK_TYPE = RegistryManager.COLLISION_ATTACK_TYPES.get(definition.getCollisionAttackTypeID());

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

		for (AttackInterceptingStateDefinition.AttackInterceptionDefinition interception : this.ACTION_DEFINITION.getAttackInterceptions(AnimationHelperImpl.INSTANCE)) {
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

			relevantInjections.removeIf(injection -> injection.predicate() != null && !injection.predicate().shouldInject(this.ID, this.CATEGORY, null));

			this.conditionallyInjectTransitions(buildingClientList, buildingServerList, relevantInjections,
					TransitionInjectionDefinition.InjectionPlacement.BEFORE, definition);
			MarioQuaMario.LOGGER.debug("Parsing transition into {}", definition.targetID());
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
				TransitionDefinition injectTransition = injection.injectedTransitionCreator().makeTransition(originalTransition, UniversalActionDefinitionHelper.INSTANCE);
				if(LOG_TRANSITION_INJECTIONS) MarioQuaMario.LOGGER.info("Injecting transition: {}->{}",
						this.ID, injectTransition.targetID());
				addTransitionToLists(buildingClientList, buildingServerList, injectTransition);
			}
		}
	}

	private void addTransitionToLists(
			List<ParsedTransition> client, List<ParsedTransition> server,
			TransitionDefinition definition
	) {
		RegistryManager.incrementTransitionCount();
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

	abstract protected ActionCategory getCategory();
}
