package com.fqf.charaformact.registries;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.definitions.states.CfaStateDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ParsedCfaState extends ParsedCfaThing {
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;
	public final float EYE_HEIGHT_FACTOR;

	private final @NotNull CfaStateDefinition STATE_DEFINITION;

	public ParsedCfaState(
			Identifier id, @NotNull CfaStateDefinition definition,
			float widthFactor, float heightFactor, float eyeHeightFactor
	) {
		super(id);
		this.STATE_DEFINITION = definition;

		this.WIDTH_FACTOR = widthFactor;
		this.HEIGHT_FACTOR = heightFactor;
		this.EYE_HEIGHT_FACTOR = eyeHeightFactor;
	}

	public void serverTick(CfaServerPlayerData data) {
		this.STATE_DEFINITION.commonTick(data);
		this.STATE_DEFINITION.serverTick(data);
	}
	public void clientTick(CfaClientData data, boolean isSelf) {
		this.STATE_DEFINITION.commonTick(data);
		this.STATE_DEFINITION.clientTick(data, isSelf);
	}

	public Set<StateAttachmentKey<?>> onEnter(CfaPlayerData data) {
		return ImmutableCollectionHelper.accumulateSet(
				builder -> this.STATE_DEFINITION.onEnter(data, builder)
		);
	}
	public void onExit(CfaPlayerData data) {
		this.STATE_DEFINITION.onExit(data);
	}

	public static void conditionallyRestoreGroundedResources(CfaPlayerData data) {
		if(data.getAction().FLAGS.contains(ActionFlag.RESTORES_GROUNDED_RESOURCES) || switch(data.getActionCategory()) {
			case GENERIC, WALLBOUND, AIRBORNE -> false;
			case GROUNDED -> true;
			case AQUATIC -> data.getPlayer().isOnGround();
			case MOUNTED -> data.getPlayer().getRootVehicle().isOnGround();
		}) restoreAllGroundedResources(data);
	}

	public static void restoreAllGroundedResources(CfaPlayerData data) {
		if(data instanceof CfaMoveableData moveableData) moveableData.jumpCapped = false;
		data.getPlayer().fallDistance = 0;

		data.getAction().restoreGroundedResources(data);
		data.getForm().restoreGroundedResources(data);
		data.getCharacter().restoreGroundedResources(data);
	}

	protected void restoreGroundedResources(CfaPlayerData data) {
		this.STATE_DEFINITION.restoreGroundedResources(data);
	}
}
