package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.actions.util.*;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugSprint extends Debug {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("debug_sprint");
	}

	@Override public void travelHook(IMarioTravelData data) {
		data.setStrafeVel(data.getInputs().getStrafeInput() * 0.5);

		double pitchRadians = Math.toRadians(data.getMario().getPitch());
		data.setForwardVel(data.getInputs().getForwardInput() * Math.cos(pitchRadians));
		data.setYVel(data.getInputs().getForwardInput() * -Math.sin(pitchRadians));
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("debug"),
						data -> !data.getMario().isSprinting(), EvaluatorContext.CLIENT_ONLY,
						null,
						(data, isSelf, seed) -> data.playSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, seed)
				)
		);
	}
}
