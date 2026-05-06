package com.fqf.charaformact.registries;

import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact_api.definitions.states.CfaStateDefinition;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import org.jetbrains.annotations.Nullable;

public class ParsedCfaState extends ParsedCfaThing {
	private final @Nullable CfaStateDefinition STATE_DEFINITION;

	private @Nullable Class<?> lastCustomVarsClass;

	public ParsedCfaState(CfaStateDefinition definition) {
		super(definition.getID());
		this.STATE_DEFINITION = definition;
	}

	public void serverTick(CfaServerPlayerData data) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.serverTick(data);
	}
	public void clientTick(CfaClientData data, boolean isSelf) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.clientTick(data, isSelf);
	}
	public Object makeCustomThing(CfaPlayerData data) {
		assert this.STATE_DEFINITION != null;
		Object vars = this.STATE_DEFINITION.provideStateData(data);
		if(vars != null) this.lastCustomVarsClass = vars.getClass();
		return vars;
	}
	public @Nullable Class<?> getLastCustomVarsClass() {
		return this.lastCustomVarsClass;
	}
}
