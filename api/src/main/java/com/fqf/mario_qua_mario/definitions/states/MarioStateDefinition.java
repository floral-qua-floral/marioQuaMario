package com.fqf.mario_qua_mario.definitions.states;

import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	@Nullable Object setupCustomMarioVars();
	void clientTick(IMarioClientData data, boolean isSelf);
	void serverTick(IMarioAuthoritativeData data);
}
