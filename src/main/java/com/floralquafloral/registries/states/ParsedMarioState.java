package com.floralquafloral.registries.states;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import net.minecraft.util.Identifier;

public abstract class ParsedMarioState {
	public final Identifier ID;
	protected final MarioStateDefinition DEFINITION;

	protected ParsedMarioState(MarioStateDefinition definition) {
		this.ID = definition.getID();
		this.DEFINITION = definition;
	}

	public void clientTick(MarioClientSideData data, boolean isSelf) {
		this.DEFINITION.clientTick(data, isSelf);
	}
	public void serverTick(MarioServerData data) {
		this.DEFINITION.serverTick(data);
	}
}
