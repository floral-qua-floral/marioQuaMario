package com.fqf.mario_qua_mario.forms;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Small implements FormDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("small");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable Identifier getReversionTarget() {
		return null;
	}
	@Override public int getValue() {
		return 0;
	}

	@Override public @Nullable SoundEvent getAcquisitionSound() {
		return null;
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 0.5F;
	}
	@Override public float getAnimationHorizontalScale() {
		return 1;
	}
	@Override public float getAnimationVerticalScale() {
		return 0.45F;
	}

	@Override public int getBapStrengthModifier() {
		return -1;
	}

	@Override public float getVoicePitch() {
		return 1.075F;
	}
	@Override public float getJumpPitch() {
		return 1.075F;
	}

	@Override public Set<String> getPowers() {
		return Set.of();
	}
	@Override public Set<AttributeModifierInstruction> getAttributeModifiers() {
		return Set.of();
	}
	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public @NotNull FormDefinition.FormHeart getFormHeart(FormHeartHelper helper) {
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

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
