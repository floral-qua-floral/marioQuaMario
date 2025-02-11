package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.definitions.states.MarioStateDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
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
