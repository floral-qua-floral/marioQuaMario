package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WaterExitJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("water_exit_jump");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	public static final CharaStat WATER_EXIT_JUMP_VEL = new CharaStat(0.939, StatCategory.JUMP_VELOCITY);

	@Override
	protected double getJumpCapThreshold() {
		return Double.POSITIVE_INFINITY;
	}

//	@Override
//	public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
//		return List.of();
//	}
}
