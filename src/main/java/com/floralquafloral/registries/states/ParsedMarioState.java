package com.floralquafloral.registries.states;

import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import net.minecraft.util.Identifier;

public abstract class ParsedMarioState {
	public final Identifier ID;
	protected final MarioStateDefinition DEFINITION;

	protected ParsedMarioState(MarioStateDefinition definition) {
		this.ID = definition.getID();
		this.DEFINITION = definition;
	}

	public void selfTick(MarioClientData data) {
		this.DEFINITION.selfTick(data);
	}
	public void otherClientsTick(MarioPlayerData data) {
		this.DEFINITION.otherClientsTick(data);
	}
	public void serverTick(MarioPlayerData data) {
		this.DEFINITION.serverTick(data);
	}
}
