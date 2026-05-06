package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.WallBodyAlignment;
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

	public float getWallYaw(CfaMoveableData data) {
		return this.WALLBOUND_DEFINITION.getWallYaw(data);
	}

	public boolean verifyWallLegality(CfaPlayerData data, Vec3d offset) {
		return this.WALLBOUND_DEFINITION.checkLegality(data, data.getWallInfo(), offset);
	}

	@Override
	public boolean travelHook(CfaMoveableData data) {
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
