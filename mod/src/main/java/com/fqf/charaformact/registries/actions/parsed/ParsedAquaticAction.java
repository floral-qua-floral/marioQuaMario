package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedAquaticAction extends AbstractParsedAction {
	private final AquaticActionDefinition AQUATIC_DEFINITION;

	public ParsedAquaticAction(AquaticActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
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
	protected void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.AQUATIC_DEFINITION.accumulateBasicTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.AQUATIC_DEFINITION.accumulateInputTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.AQUATIC_DEFINITION.accumulateCollisionTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}
}