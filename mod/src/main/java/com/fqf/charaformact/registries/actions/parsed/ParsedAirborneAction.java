package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedAirborneAction extends AbstractParsedAction {
	private final AirborneActionDefinition AIRBORNE_DEFINITION;

	public ParsedAirborneAction(AirborneActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
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
	protected void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.AIRBORNE_DEFINITION.accumulateBasicTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.AIRBORNE_DEFINITION.accumulateInputTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.AIRBORNE_DEFINITION.accumulateCollisionTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}
}
