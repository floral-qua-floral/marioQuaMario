package com.fqf.charapoweract_api.definitions.states;

import com.fqf.charapoweract_api.mariodata.IMarioAuthoritativeData;
import com.fqf.charapoweract_api.mariodata.IMarioClientData;
import com.fqf.charapoweract_api.mariodata.IMarioData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	@Nullable Object setupCustomMarioVars(IMarioData data);
	void clientTick(IMarioClientData data, boolean isSelf);
	void serverTick(IMarioAuthoritativeData data);
}
