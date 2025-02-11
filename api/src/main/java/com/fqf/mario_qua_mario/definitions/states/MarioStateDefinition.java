package com.fqf.mario_qua_mario.definitions.states;

import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	@Nullable Object setupCustomMarioVars(IMarioData data);
	void clientTick(IMarioClientData data, boolean isSelf);
	void serverTick(IMarioAuthoritativeData data);
}
