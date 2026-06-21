package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.SlidingStatus;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class DebugVanillaTravel extends Debug implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("debug_vanilla_travel");

	@Override
	public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override
	public boolean travelHook(CfaTravelData data) {
		return false;
	}
}
