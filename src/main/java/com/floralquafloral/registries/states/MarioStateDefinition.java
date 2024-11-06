package com.floralquafloral.registries.states;

import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	void clientTick(MarioPlayerData data, boolean isSelf);
	void serverTick(MarioPlayerData data);
}
