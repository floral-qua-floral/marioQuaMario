package com.fqf.charaformact.registries;

import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.definitions.states.CfaStateDefinition;
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
}
