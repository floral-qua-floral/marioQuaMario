package com.fqf.charaformact.registries.actions;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.registries.*;
import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.MountedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractParsedAction extends ParsedCfaState implements ParsedAttackInterceptingState {
	protected final IncompleteActionDefinition ACTION_DEFINITION;
	public final ActionCategory CATEGORY;

	public final @Nullable ParsedAnimation ANIMATION;
	public final @Nullable CameraAnimationSet CAMERA_ANIMATIONS;
	public final SlidingStatus SLIDING_STATUS;

	public final SneakingRule SNEAKING_RULE;
	public final SprintingRule SPRINTING_RULE;
	public final @NotNull GenericActionType GENERIC_ACTION_TYPE;

	public final @NotNull BappingRule BAPPING_RULE;
	public final @Nullable ParsedCollisionAttack COLLISION_ATTACK_TYPE;

	public final Map<AbstractParsedAction, ParsedTransition> TRANSITIONS_FROM_TARGETS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> CLIENT_TRANSITIONS;
	public final EnumMap<TransitionPhase, List<ParsedTransition>> SERVER_TRANSITIONS;

	private final List<ParsedAttackInterception> INTERCEPTIONS_INTERNAL, INTERCEPTIONS_VIEW;

	private static final boolean LOG_TRANSITION_INJECTIONS = CharaFormAct.CONFIG.logActionTransitionInjections();

	private static final BappingRule NULL_EQUIVALENT = new BappingRule(0, 0);

	public AbstractParsedAction(Identifier id, IncompleteActionDefinition definition) {
		super(id, definition);

		CharaFormAct.LOGGER.info("Parsing action {}...", this.ID);

		this.ACTION_DEFINITION = definition;
		this.CATEGORY = this.getCategory();

		AnimationDefinition animation = definition.defineAnimation();
		if(animation == null) this.ANIMATION = null;
		else this.ANIMATION = new ParsedAnimation(animation);
		this.CAMERA_ANIMATIONS = definition.defineCameraAnimations(AnimationHelperImpl.INSTANCE);
		this.SLIDING_STATUS = definition.defineSlidingStatus();

		this.SNEAKING_RULE = definition.defineSneakingRule();
		this.SPRINTING_RULE = definition.defineSprintingRule();
		this.GENERIC_ACTION_TYPE = switch (definition) {
			case GenericActionDefinition genericDefinition -> genericDefinition.getGenericActionType();
			case MountedActionDefinition ignored -> GenericActionType.VANILLA_TRAVEL;
			default -> GenericActionType.UNSPECIFIED;
		};

		BappingRule bappingRule = definition.defineBappingRule();
		this.BAPPING_RULE = bappingRule == null ? NULL_EQUIVALENT : bappingRule;

		Identifier collisionAttackID = definition.defineActiveCollisionAttack();
		if(collisionAttackID == null)
			this.COLLISION_ATTACK_TYPE = null;
		else
			this.COLLISION_ATTACK_TYPE = Objects.requireNonNull(RegistryManager.COLLISION_ATTACKS.get(collisionAttackID),
					"Action " + this.ID + " tries to use unregistered Collision Attack " + collisionAttackID + "!");

		this.TRANSITIONS_FROM_TARGETS = new HashMap<>();
		this.CLIENT_TRANSITIONS = new EnumMap<>(TransitionPhase.class);
		this.SERVER_TRANSITIONS = new EnumMap<>(TransitionPhase.class);

		this.INTERCEPTIONS_INTERNAL = new ArrayList<>();
		this.INTERCEPTIONS_VIEW = Collections.unmodifiableList(this.INTERCEPTIONS_INTERNAL);
	}

	protected abstract void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper);
	protected abstract void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper);
	protected abstract void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper);

	public void parseTransitions(List<TransitionInjectionDefinition> injections) {
		UniversalActionTransitionHelper helper = new UniversalActionTransitionHelper(this);
		this.parseTransitions(
				TransitionPhase.BASIC,
				ImmutableCollectionHelper.accumulateList(builder -> accumulateBasicTransitions(builder, helper)),
				injections
		);
		this.parseTransitions(
				TransitionPhase.INPUT,
				ImmutableCollectionHelper.accumulateList(builder -> accumulateInputTransitions(builder, helper)),
				injections
		);
		this.parseTransitions(
				TransitionPhase.WORLD_COLLISION,
				ImmutableCollectionHelper.accumulateList(builder -> accumulateCollisionTransitions(builder, helper)),
				injections
		);

		List<AttackInterceptingStateDefinition.AttackInterceptionDefinition> interceptionDefinitions;
		interceptionDefinitions = ImmutableCollectionHelper.accumulateList(builder -> this.ACTION_DEFINITION.accumulateAttackInterceptions(builder, AnimationHelperImpl.INSTANCE));
		this.INTERCEPTIONS_INTERNAL.addAll(interceptionDefinitions.stream().map(definition -> new ParsedAttackInterception(definition, true)).toList());
	}
	private void parseTransitions(
			TransitionPhase phase, List<ActionTransitionDetails> transitionDefinitions,
			List<TransitionInjectionDefinition> injections
	) {
		ImmutableList.Builder<ParsedTransition> clientBuilder = new ImmutableList.Builder<>();
		ImmutableList.Builder<ParsedTransition> serverBuilder = new ImmutableList.Builder<>();

//		this.CLIENT_TRANSITIONS.putIfAbsent(phase, new ArrayList<>());
//		this.SERVER_TRANSITIONS.putIfAbsent(phase, new ArrayList<>());
//		List<ParsedTransition> buildingClientList = this.CLIENT_TRANSITIONS.get(phase);
//		List<ParsedTransition> buildingServerList = this.SERVER_TRANSITIONS.get(phase);

		for(ActionTransitionDetails details : transitionDefinitions) {
			ImmutableList.Builder<ActionTransitionDetails> afterInjections = ImmutableList.builder();

			@Nullable AbstractParsedAction transitioningTo = RegistryManager.ACTIONS.get(details.targetID());

			if(transitioningTo == null) throw new IllegalArgumentException("Action " + this.ID + " contains a transition to "
					+ details.targetID() + ", which is not registered!!");

			for (TransitionInjectionDefinition injection : injections) {
				TransitionInjectionDefinition.InjectionPlacement placement = injection.getPlacementRelativeTo(this.getCategory(), this.ID, transitioningTo.CATEGORY, transitioningTo.ID);
				if(placement == null) continue;

				ActionTransitionDetails injectionDetails = injection.makeTransition(details, new UniversalActionTransitionHelper(this));
				if(placement == TransitionInjectionDefinition.InjectionPlacement.BEFORE)
					this.injectTransitionToBuilders(clientBuilder, serverBuilder, injectionDetails,
							TransitionInjectionDefinition.InjectionPlacement.BEFORE, transitioningTo.ID);
				else afterInjections.add(injectionDetails);
			}

			this.addTransitionToBuilders(clientBuilder, serverBuilder, details);

			for(ActionTransitionDetails injectionDetails : afterInjections.build()) {
				this.injectTransitionToBuilders(clientBuilder, serverBuilder, injectionDetails,
						TransitionInjectionDefinition.InjectionPlacement.AFTER, transitioningTo.ID);
			}
		}

		this.CLIENT_TRANSITIONS.put(phase, clientBuilder.build());
		this.SERVER_TRANSITIONS.put(phase, serverBuilder.build());
	}

	private void injectTransitionToBuilders(
			ImmutableList.Builder<ParsedTransition> client, ImmutableList.Builder<ParsedTransition> server,
			ActionTransitionDetails injectionDetails, TransitionInjectionDefinition.InjectionPlacement placement,
			Identifier naturalTarget
	) {
		if(LOG_TRANSITION_INJECTIONS)
			CharaFormAct.LOGGER.info("Injecting transition *->{} {} natural transition {}->{}",
					injectionDetails.targetID(), placement, this.ID, naturalTarget);
		this.addTransitionToBuilders(client, server, injectionDetails);
	}
	private void addTransitionToBuilders(
			ImmutableList.Builder<ParsedTransition> client, ImmutableList.Builder<ParsedTransition> server,
			ActionTransitionDetails definition
	) {
		RegistryManager.incrementTransitionCount();
		ParsedTransition transition = new ParsedTransition(definition);
		if(this.TRANSITIONS_FROM_TARGETS.containsKey(transition.targetAction()))
			CharaFormAct.LOGGER.warn("Action {} has multiple transitionDefinitions into {}! This is likely to cause issues!",
					this.ID, transition.targetAction().ID);
		else this.TRANSITIONS_FROM_TARGETS.put(transition.targetAction(), transition);
		if(definition.environment().CHECK_ON_CLIENT) client.add(transition);
		if(definition.environment().CHECK_ON_SERVER) server.add(transition);
	}

	public int getIntID() {
		return RegistryManager.ACTIONS.getRawIdOrThrow(this);
	}

	abstract public boolean travelHook(CfaMoveableData data);

	abstract protected ActionCategory getCategory();

	@Override
	public List<ParsedAttackInterception> getInterceptions() {
		return INTERCEPTIONS_VIEW;
	}
}
