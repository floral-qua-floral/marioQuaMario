package com.floralquafloral.registries.states;

import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	void selfTick(MarioClientData data);
	void otherClientsTick(MarioPlayerData data);
	void serverTick(MarioPlayerData data);
}
