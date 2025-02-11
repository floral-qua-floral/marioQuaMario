package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class TailSpinJump extends TailSpinFall implements AirborneActionDefinition {
	// This only exists to get around the limitations of the action transition system
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("tail_spin_jump");
	}

	public static final CharaStat JUMP_VEL = Jump.JUMP_VEL.variate(0.7, DUCKING, JUMP_VELOCITY, POWER_UP);
}
