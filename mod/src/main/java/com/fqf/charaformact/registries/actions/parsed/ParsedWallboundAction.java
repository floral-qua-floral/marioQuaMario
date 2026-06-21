package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.registries.actions.UniversalActionTransitionHelper;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.WallBodyAlignment;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class ParsedWallboundAction extends AbstractParsedAction {
	private final WallboundActionDefinition WALLBOUND_DEFINITION;

	public final WallBodyAlignment ALIGNMENT;
	public final float HEAD_RANGE;

	public ParsedWallboundAction(Identifier id, WallboundActionDefinition definition) {
		super(id, definition);
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
	protected void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper) {
		this.WALLBOUND_DEFINITION.accumulateBasicTransitions(builder, helper);
	}

	@Override
	protected void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper) {
		this.WALLBOUND_DEFINITION.accumulateInputTransitions(builder, helper);
	}

	@Override
	protected void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, UniversalActionTransitionHelper helper) {
		this.WALLBOUND_DEFINITION.accumulateCollisionTransitions(builder, helper);
	}
}
