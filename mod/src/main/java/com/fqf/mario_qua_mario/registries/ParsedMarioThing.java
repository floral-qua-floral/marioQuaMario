package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.definitions.MarioStateDefinition;
import net.minecraft.util.Identifier;

public class ParsedMarioThing {
	public final Identifier ID;

	public ParsedMarioThing(MarioStateDefinition definition) {
		this.ID	= definition.getID();
	}

	// TODO: Additional constructor for use with Stomp Types
}
