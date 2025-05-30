package com.fqf.mario_qua_mario_content.actions.power;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Jump;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class TailSpinJump extends TailSpinFall implements AirborneActionDefinition {
	// This only exists to get around the limitations of the action transition system
	public static final Identifier ID = MarioQuaMarioContent.makeID("tail_spin_jump");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	public static final CharaStat JUMP_VEL = Jump.JUMP_VEL.variate(0.7, DUCKING, JUMP_VELOCITY, POWER_UP);
}
