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
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Set;

public class ParsedWallboundAction extends AbstractParsedAction {
	private final WallboundActionDefinition WALLBOUND_DEFINITION;

	public final WallBodyAlignment ALIGNMENT;
	public final float HEAD_RANGE;

	public ParsedWallboundAction(WallboundActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.WALLBOUND_DEFINITION = definition;

		this.ALIGNMENT = definition.defineBodyAlignment();
		this.HEAD_RANGE = definition.defineHeadYawRange();
	}

	public float getWallYaw(CfaMoveableData data) {
		return this.WALLBOUND_DEFINITION.calculateWallYaw(data);
	}

	public boolean verifyWallLegality(CfaPlayerData data, Vec3d offset) {
		return this.WALLBOUND_DEFINITION.verifyLegality(data, data.getWallInfo(), offset);
	}

	@Override
	public boolean travelHook(CfaMoveableData data) {
		data.jumpCapped = false;
		UniversalActionDefinitionHelper helper = UniversalActionDefinitionHelper.INSTANCE;
		this.WALLBOUND_DEFINITION.travel(data, helper.getWallInfo(data), helper);
		data.applyLevitation();
		return true;
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.WALLBOUND;
	}

	@Override
	protected void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.WALLBOUND_DEFINITION.accumulateBasicTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.WALLBOUND_DEFINITION.accumulateInputTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder) {
		this.WALLBOUND_DEFINITION.accumulateCollisionTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}
}
