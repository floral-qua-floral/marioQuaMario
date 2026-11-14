package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;

public class DebugVanillaTravel extends Debug implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("debug_vanilla_travel");

	@Override
	public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.add(ActionFlag.DO_VANILLA_TRAVEL, ActionFlag.ALLOW_STEALTH);
	}

	@Override
	public void travelHook(CfaTravelData data) {

	}
}
