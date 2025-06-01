package com.fqf.mario_qua_mario_content.actions.generic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SlidingStatus;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugVanillaTravel extends Debug implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("debug_vanilla_travel");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override
	public boolean travelHook(IMarioTravelData data) {
		return false;
	}

	@Override
	public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of();
	}
}
