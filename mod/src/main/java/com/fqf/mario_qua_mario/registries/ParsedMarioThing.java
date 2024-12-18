package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario.definitions.states.MarioStateDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import net.minecraft.util.Identifier;

public class ParsedMarioThing {
	private final MarioStateDefinition DEFINITION;
	public final Identifier ID;

	public ParsedMarioThing(MarioStateDefinition definition) {
		this.DEFINITION = definition;
		this.ID	= definition.getID();
	}

	public ParsedMarioThing(StompTypeDefinition definition) {
		this.DEFINITION = null;
		this.ID = definition.getID();
	}

	public void serverTick(MarioServerPlayerData data) {
		this.DEFINITION.serverTick(data);
	}
	public void clientTick(IMarioClientData data, boolean isSelf) {
		this.DEFINITION.clientTick(data, isSelf);
	}

	// TODO: Additional constructor for use with Stomp Types
}
