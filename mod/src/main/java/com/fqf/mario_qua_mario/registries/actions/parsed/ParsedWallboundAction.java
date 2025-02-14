package com.fqf.mario_qua_mario.registries.actions.parsed;

import com.fqf.mario_qua_mario.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.UniversalActionDefinitionHelper;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedWallboundAction extends AbstractParsedAction {
	private final WallboundActionDefinition WALLBOUND_DEFINITION;

	public ParsedWallboundAction(WallboundActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.WALLBOUND_DEFINITION = definition;
	}

	@Override
	public boolean travelHook(MarioMoveableData data) {
		UniversalActionDefinitionHelper helper = UniversalActionDefinitionHelper.INSTANCE;
		this.WALLBOUND_DEFINITION.travelHook(data, helper.getWallInfo(data), helper);
		return true;
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.WALLBOUND;
	}

	@Override
	protected List<TransitionDefinition> getBasicTransitions() {
		return this.WALLBOUND_DEFINITION.getBasicTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getInputTransitions() {
		return this.WALLBOUND_DEFINITION.getInputTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getWorldCollisionTransitions() {
		return this.WALLBOUND_DEFINITION.getWorldCollisionTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}
}
