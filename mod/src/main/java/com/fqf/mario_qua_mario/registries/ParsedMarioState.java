package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario.definitions.states.MarioStateDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParsedMarioState extends ParsedMarioThing {
	private final @Nullable MarioStateDefinition STATE_DEFINITION;

	public final @Nullable Class<?> CUSTOM_DATA_CLASS;

	public ParsedMarioState(MarioStateDefinition definition) {
		super(definition.getID());
		this.STATE_DEFINITION = definition;
		Object customVars = definition.setupCustomMarioVars();
		this.CUSTOM_DATA_CLASS = customVars == null ? null : customVars.getClass();
	}

	public void serverTick(MarioServerPlayerData data) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.serverTick(data);
	}
	public void clientTick(IMarioClientData data, boolean isSelf) {
		assert this.STATE_DEFINITION != null;
		this.STATE_DEFINITION.clientTick(data, isSelf);
	}
	public Object makeCustomThing() {
		assert this.STATE_DEFINITION != null;
		return this.STATE_DEFINITION.setupCustomMarioVars();
	}
}
