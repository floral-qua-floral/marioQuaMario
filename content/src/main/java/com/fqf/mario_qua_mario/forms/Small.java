package com.fqf.mario_qua_mario.forms;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Small implements FormDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("small");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override public int defineValue() {
		return 0;
	}

	@Override public float defineHeightFactor() {
		return 0.5F;
	}
	@Override public float defineAnimationVerticalScale() {
		return 0.45F;
	}

	@Override public int defineBapStrengthModifier() {
		return -1;
	}

	@Override public float defineVoicePitch() {
		return 1.075F;
	}
	@Override public float defineJumpPitch() {
		return 1.075F;
	}

	@Override public @NotNull FormDefinition.FormHeart defineFormHeart(FormHeartHelper helper) {
		return new FormHeart(
				MarioQuaMario.makeResID("hud/form_hearts/small/full"),
				MarioQuaMario.makeResID("hud/form_hearts/small/full_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/small/half"),
				MarioQuaMario.makeResID("hud/form_hearts/small/half_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/small/hardcore/full"),
				MarioQuaMario.makeResID("hud/form_hearts/small/hardcore/full_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/small/hardcore/half"),
				MarioQuaMario.makeResID("hud/form_hearts/small/hardcore/half_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/small/container"),
				MarioQuaMario.makeResID("hud/form_hearts/small/container_blinking")
		);
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}

}
