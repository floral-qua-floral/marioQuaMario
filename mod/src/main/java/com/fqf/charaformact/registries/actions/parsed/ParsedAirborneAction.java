package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.google.common.collect.ImmutableList;

public class ParsedAirborneAction extends AbstractParsedAction {
	private final AirborneActionDefinition AIRBORNE_DEFINITION;

	public ParsedAirborneAction(AirborneActionDefinition definition) {
		super(definition);
		this.AIRBORNE_DEFINITION = definition;
	}

	@Override
	public boolean travelHook(CfaMoveableData data) {
		this.AIRBORNE_DEFINITION.travelHook(data, UniversalActionDefinitionHelper.INSTANCE);
		if(data.getYVel() > 0) data.getPlayer().fallDistance = 0;
		data.applyLevitation();
		return true;
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.AIRBORNE;
	}

	@Override
	protected void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.AIRBORNE_DEFINITION.accumulateBasicTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.AIRBORNE_DEFINITION.accumulateInputTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.AIRBORNE_DEFINITION.accumulateCollisionTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}
}
