package com.fqf.mario_qua_mario.registries.actions.parsed;

import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.StatCategory;
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

	private static final CharaStat GROUNDED_GRAVITY = new CharaStat(-0.115, StatCategory.NORMAL_GRAVITY);
	private static final CharaStat GROUNDED_TERMINAL_VELOCITY = new CharaStat(-0.5, StatCategory.TERMINAL_VELOCITY);

	@Override
	public void travelHook(MarioMoveableData data) {
		this.GROUNDED_DEFINITION.travelHook(data, UniversalActionDefinitionHelper.INSTANCE);
		if(data.isClient())
			UniversalActionDefinitionHelper.INSTANCE.applyGravity(data, GROUNDED_GRAVITY, GROUNDED_TERMINAL_VELOCITY);
		else data.setYVel(-0.1);
	}

	@Override
	protected TransitionInjectionDefinition.ActionCategory getCategory() {
		return TransitionInjectionDefinition.ActionCategory.GROUNDED;
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
