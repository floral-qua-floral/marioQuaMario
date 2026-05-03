package com.fqf.charapoweract.registries.actions.parsed;

import com.fqf.charapoweract.cpadata.CPAMoveableData;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charapoweract.registries.actions.AbstractParsedAction;
import com.fqf.charapoweract.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.charapoweract_api.definitions.states.actions.util.WallBodyAlignment;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedWallboundAction extends AbstractParsedAction {
	private final WallboundActionDefinition WALLBOUND_DEFINITION;

	public final WallBodyAlignment ALIGNMENT;
	public final float HEAD_RANGE;

	public ParsedWallboundAction(WallboundActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.WALLBOUND_DEFINITION = definition;

		this.ALIGNMENT = definition.getBodyAlignment();
		this.HEAD_RANGE = definition.getHeadYawRange();
	}

	public float getWallYaw(CPAMoveableData data) {
		return this.WALLBOUND_DEFINITION.getWallYaw(data);
	}

	public boolean verifyWallLegality(CPAPlayerData data, Vec3d offset) {
		return this.WALLBOUND_DEFINITION.checkLegality(data, data.getWallInfo(), offset);
	}

	@Override
	public boolean travelHook(CPAMoveableData data) {
		data.jumpCapped = false;
		UniversalActionDefinitionHelper helper = UniversalActionDefinitionHelper.INSTANCE;
		this.WALLBOUND_DEFINITION.travelHook(data, helper.getWallInfo(data), helper);
		data.applyLevitation();
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
