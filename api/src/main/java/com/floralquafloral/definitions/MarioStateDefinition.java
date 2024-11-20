package com.floralquafloral.definitions;

import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	void clientTick(MarioClientSideData data, boolean isSelf);
	void serverTick(MarioAuthoritativeData data);
}
