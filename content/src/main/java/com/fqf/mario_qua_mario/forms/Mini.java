package com.fqf.mario_qua_mario.forms;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.Powers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
import static net.minecraft.entity.attribute.EntityAttributes.*;

public class Mini implements FormDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("mini");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override public @Nullable Identifier defineReversionTarget() {
		return null;
	}
	@Override public int defineValue() {
		return 0;
	}

	@Override public @Nullable SoundEvent defineAcquisitionSound() {
		return MarioSFX.MINI_EMPOWER;
	}

	@Override public float defineWidthFactor() {
		return 0.5F;
	}
	@Override public float defineHeightFactor() {
		return 0.25F;
	}
	@Override public float defineEyeHeightFactor() {
		return 0.2F;
	}

	@Override public float defineAnimationHorizontalScale() {
		return 0.5F;
	}
	@Override public float defineAnimationVerticalScale() {
		return 0.25F;
	}

	@Override public int defineBapStrengthModifier() {
		return -2;
	}

	@Override public float defineVoicePitch() {
		return 1.545F;
	}
	@Override public float defineJumpPitch() {
		return 1.5F;
	}

	@Override public void accumulatePowers(ImmutableSet.Builder<String> builder) {
		builder.add(
				Powers.SPRINT_ON_WATER,
				Powers.TALLER_SOLID_WATER_HITBOX
		);
	}
	@Override public void accumulateAttributeModifiers(ImmutableSet.Builder<AttributeModifierInstruction> builder) {
		builder.add(
				new AttributeModifierInstruction(GENERIC_SAFE_FALL_DISTANCE, 0.5, ADD_MULTIPLIED_TOTAL),
				new AttributeModifierInstruction(GENERIC_STEP_HEIGHT, -0.5, ADD_MULTIPLIED_TOTAL)
		);
	}
	@Override public void accumulateStatModifiers(ImmutableSet.Builder<StatModifier> builder) {
		builder.add(
				new StatModifier(Set.of(StatCategory.NORMAL_GRAVITY), 0.6),
				new StatModifier(Set.of(StatCategory.JUMPING_GRAVITY), 0.375),
				new StatModifier(Set.of(StatCategory.JUMP_VELOCITY), 0.7),

				new StatModifier(Set.of(StatCategory.COLLISION_ATTACK, StatCategory.DAMAGE), (base, categories) -> base * 0.5 - 3)
		);
	}

	@Override public @NotNull FormDefinition.FormHeart defineFormHeart(FormHeartHelper helper) {
		return new FormHeart(
				MarioQuaMario.makeResID("hud/form_hearts/mini/full"),
				MarioQuaMario.makeResID("hud/form_hearts/mini/full_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/mini/half"),
				MarioQuaMario.makeResID("hud/form_hearts/mini/half_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/mini/hardcore/full"),
				MarioQuaMario.makeResID("hud/form_hearts/mini/hardcore/full_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/mini/hardcore/half"),
				MarioQuaMario.makeResID("hud/form_hearts/mini/hardcore/half_blinking"),

				MarioQuaMario.makeResID("hud/form_hearts/mini/container"),
				MarioQuaMario.makeResID("hud/form_hearts/mini/container_blinking")
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
