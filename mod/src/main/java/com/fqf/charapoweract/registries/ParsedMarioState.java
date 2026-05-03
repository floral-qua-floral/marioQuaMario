package com.fqf.charapoweract.registries;

import com.fqf.charapoweract_api.definitions.states.MarioStateDefinition;
import com.fqf.charapoweract_api.mariodata.IMarioClientData;
import com.fqf.charapoweract.mariodata.MarioPlayerData;
import com.fqf.charapoweract.mariodata.MarioServerPlayerData;
import org.jetbrains.annotations.Nullable;

public class ParsedMarioState extends ParsedMarioThing {
	private final @Nullable MarioStateDefinition STATE_DEFINITION;

	private @Nullable Class<?> lastCustomVarsClass;

	public ParsedMarioState(MarioStateDefinition definition) {
		super(definition.getID());
		this.STATE_DEFINITION = definition;
	}

	public void serverTick(MarioServerPlayerData data) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.serverTick(data);
	}
	public void clientTick(IMarioClientData data, boolean isSelf) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.clientTick(data, isSelf);
	}
	public Object makeCustomThing(MarioPlayerData data) {
		assert this.STATE_DEFINITION != null;
		Object vars = this.STATE_DEFINITION.setupCustomMarioVars(data);
		if(vars != null) this.lastCustomVarsClass = vars.getClass();
		return vars;
	}
	public @Nullable Class<?> getLastCustomVarsClass() {
		return this.lastCustomVarsClass;
	}
}
