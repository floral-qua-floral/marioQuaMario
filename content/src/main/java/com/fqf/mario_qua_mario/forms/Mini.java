package com.fqf.mario_qua_mario.forms;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Mini implements FormDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("mini");
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
		return MarioSFX.MINI_EMPOWER;
	}

	@Override public float getWidthFactor() {
		return 0.5F;
	}
	@Override public float getHeightFactor() {
		return 0.25F;
	}
	@Override public float getEyeHeightFactor() {
		return 0.2F;
	}

	@Override public float getAnimationHorizontalScale() {
		return 0.5F;
	}
	@Override public float getAnimationVerticalScale() {
		return 0.25F;
	}

	@Override public int getBapStrengthModifier() {
		return -2;
	}

	@Override public float getVoicePitch() {
		return 1.545F;
	}
	@Override public float getJumpPitch() {
		return 1.5F;
	}

	@Override public Set<String> getPowers() {
		return Set.of(
				Powers.SPRINT_ON_WATER,
				Powers.TALLER_SOLID_WATER_HITBOX
		);
	}
	@Override public Set<AttributeModifierInstruction> getAttributeModifiers() {
		return Set.of(
				new AttributeModifierInstruction(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
				new AttributeModifierInstruction(EntityAttributes.GENERIC_STEP_HEIGHT, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
		);
	}
	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of(
				new StatModifier(Set.of(StatCategory.NORMAL_GRAVITY), 0.6),
				new StatModifier(Set.of(StatCategory.JUMPING_GRAVITY), 0.375),
				new StatModifier(Set.of(StatCategory.JUMP_VELOCITY), 0.7),

				new StatModifier(Set.of(StatCategory.COLLISION_ATTACK, StatCategory.DAMAGE), (base, categories) -> base * 0.5 - 3)
		);
	}

	@Override public @NotNull FormDefinition.FormHeart getFormHeart(FormHeartHelper helper) {
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

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
