package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.StatCategory;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedGroundedAction extends AbstractParsedAction {
	private final GroundedActionDefinition GROUNDED_DEFINITION;

	public ParsedGroundedAction(GroundedActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.GROUNDED_DEFINITION = definition;
	}

	private static final CfaStat GROUNDED_GRAVITY = new CfaStat(-0.115, StatCategory.NORMAL_GRAVITY);
	private static final CfaStat GROUNDED_TERMINAL_VELOCITY = new CfaStat(-0.5, StatCategory.TERMINAL_VELOCITY);

	@Override
	public boolean travelHook(CfaMoveableData data) {
		data.jumpCapped = false;
		this.GROUNDED_DEFINITION.travelHook(data, UniversalActionDefinitionHelper.INSTANCE);
		if(data.isClient() || data.getYVel() > 0)
			UniversalActionDefinitionHelper.INSTANCE.applyGravity(data, GROUNDED_GRAVITY, GROUNDED_TERMINAL_VELOCITY);
		else
			if(data.getYVel() <= 0) data.setYVel(-0.1);
		if(data.applyLevitation() && data.getYVel() < 0.05) data.setYVel(0.05);
		return true;
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.GROUNDED;
	}

	@Override
	protected List<TransitionDefinition> getBasicTransitions() {
		return this.GROUNDED_DEFINITION.getBasicTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getInputTransitions() {
		return this.GROUNDED_DEFINITION.getInputTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getWorldCollisionTransitions() {
		return this.GROUNDED_DEFINITION.getWorldCollisionTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}
}
