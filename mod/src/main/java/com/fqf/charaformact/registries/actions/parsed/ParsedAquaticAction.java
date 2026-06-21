package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.google.common.collect.ImmutableList;

public class ParsedAquaticAction extends AbstractParsedAction {
	private final AquaticActionDefinition AQUATIC_DEFINITION;

	public ParsedAquaticAction(AquaticActionDefinition definition) {
		super(definition);
		this.AQUATIC_DEFINITION = definition;
	}

	@Override
	public boolean travelHook(CfaMoveableData data) {
		data.jumpCapped = false;
		this.AQUATIC_DEFINITION.travelHook(data, UniversalActionDefinitionHelper.INSTANCE);
		return true;
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.AQUATIC;
	}

	@Override
	protected void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.AQUATIC_DEFINITION.accumulateBasicTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.AQUATIC_DEFINITION.accumulateInputTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.AQUATIC_DEFINITION.accumulateCollisionTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}
}