package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.charaformact.registries.actions.UniversalActionTransitionHelper;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;

public class ParsedGenericAction extends AbstractParsedAction {
	private final GenericActionDefinition GENERIC_DEFINITION;

	public ParsedGenericAction(Identifier id, GenericActionDefinition definition) {
		super(id, definition);
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

	@Override protected void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper) {
		this.GENERIC_DEFINITION.accumulateBasicTransitions(builder, helper);
	}

	@Override protected void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper) {
		this.GENERIC_DEFINITION.accumulateInputTransitions(builder, helper);
	}

	@Override protected void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper) {
		this.GENERIC_DEFINITION.accumulateCollisionTransitions(builder, helper);
	}
}
