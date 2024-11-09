package com.floralquafloral.registries.states;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	void clientTick(MarioClientSideData data, boolean isSelf);
	void serverTick(MarioServerData data);
}
