package com.fqf.charapoweract.registries;

import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract.cpadata.CPAServerPlayerData;
import com.fqf.charapoweract_api.definitions.states.CPAStateDefinition;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import org.jetbrains.annotations.Nullable;

public class ParsedCPAState extends ParsedCPAThing {
	private final @Nullable CPAStateDefinition STATE_DEFINITION;

	private @Nullable Class<?> lastCustomVarsClass;

	public ParsedCPAState(CPAStateDefinition definition) {
		super(definition.getID());
		this.STATE_DEFINITION = definition;
	}

	public void serverTick(CPAServerPlayerData data) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.serverTick(data);
	}
	public void clientTick(ICPAClientData data, boolean isSelf) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.clientTick(data, isSelf);
	}
	public Object makeCustomThing(CPAPlayerData data) {
		assert this.STATE_DEFINITION != null;
		Object vars = this.STATE_DEFINITION.provideStateData(data);
		if(vars != null) this.lastCustomVarsClass = vars.getClass();
		return vars;
	}
	public @Nullable Class<?> getLastCustomVarsClass() {
		return this.lastCustomVarsClass;
	}
}
