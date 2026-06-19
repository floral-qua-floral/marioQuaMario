package com.fqf.mario_qua_mario.forms;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Super implements FormDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("super");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override public @Nullable Identifier defineReversionTarget() {
		return Small.ID;
	}

	@Override public @Nullable SoundEvent defineReversionSound() {
		return MarioSFX.REVERT;
	}
	@Override public @Nullable SoundEvent defineAcquisitionSound() {
		return MarioSFX.EMPOWER;
	}
}
