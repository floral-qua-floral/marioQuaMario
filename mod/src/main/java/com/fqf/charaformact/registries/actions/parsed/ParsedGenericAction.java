package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Set;

public class ParsedGenericAction extends AbstractParsedAction {
	private final GenericActionDefinition GENERIC_DEFINITION;

	public ParsedGenericAction(GenericActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.GENERIC_DEFINITION = definition;
	}

	@Override
	public boolean travelHook(CfaMoveableData data) {
		data.jumpCapped = false;
		return this.GENERIC_DEFINITION.travelHook(data);
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.GENERIC;
	}

	@Override protected void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.GENERIC_DEFINITION.accumulateBasicTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override protected void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.GENERIC_DEFINITION.accumulateInputTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override protected void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.GENERIC_DEFINITION.accumulateCollisionTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}
}
