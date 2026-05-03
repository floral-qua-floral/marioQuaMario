package com.fqf.charapoweract.registries.actions.parsed;

import com.fqf.charapoweract_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charapoweract.cpadata.CPAMoveableData;
import com.fqf.charapoweract.registries.actions.AbstractParsedAction;
import com.fqf.charapoweract.registries.actions.UniversalActionDefinitionHelper;
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
	public boolean travelHook(CPAMoveableData data) {
		data.jumpCapped = false;
		this.AQUATIC_DEFINITION.travelHook(data, UniversalActionDefinitionHelper.INSTANCE);
		return true;
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.AQUATIC;
	}

	@Override
	protected List<TransitionDefinition> getBasicTransitions() {
		return this.AQUATIC_DEFINITION.getBasicTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getInputTransitions() {
		return this.AQUATIC_DEFINITION.getInputTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getWorldCollisionTransitions() {
		return this.AQUATIC_DEFINITION.getWorldCollisionTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}
}