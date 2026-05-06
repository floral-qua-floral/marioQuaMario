package com.fqf.mario_qua_mario.powerups;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
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
		return null;
	}

	@Override public float getWidthFactor() {
		return 0.5F;
	}
	@Override public float getHeightFactor() {
		return 0.25F;
	}
	@Override public float getAnimationWidthFactor() {
		return 0.5F;
	}
	@Override public float getAnimationHeightFactor() {
		return 0.25F;
	}

	@Override public int getBumpStrengthModifier() {
		return -2;
	}

	@Override public float getVoicePitch() {
		return 1.545F;
	}
	@Override public float getJumpPitch() {
		return 1.5F;
	}

	@Override public Set<String> getPowers() {
		return Set.of();
	}

	@Override public @NotNull FormDefinition.FormHeart getFormHeart(FormHeartHelper helper) {
		return new FormHeart(
				MarioQuaMario.makeResID("hud/power_hearts/small/full"),
				MarioQuaMario.makeResID("hud/power_hearts/small/full_blinking"),

				MarioQuaMario.makeResID("hud/power_hearts/small/half"),
				MarioQuaMario.makeResID("hud/power_hearts/small/half_blinking"),

				MarioQuaMario.makeResID("hud/power_hearts/small/hardcore/full"),
				MarioQuaMario.makeResID("hud/power_hearts/small/hardcore/full_blinking"),

				MarioQuaMario.makeResID("hud/power_hearts/small/hardcore/half"),
				MarioQuaMario.makeResID("hud/power_hearts/small/hardcore/half_blinking"),

				MarioQuaMario.makeResID("hud/power_hearts/small/container"),
				MarioQuaMario.makeResID("hud/power_hearts/small/container_blinking")
		);
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of(
				new StatModifier(Set.of(StatCategory.NORMAL_GRAVITY), 0.6),
				new StatModifier(Set.of(StatCategory.JUMPING_GRAVITY), 0.375),
				new StatModifier(Set.of(StatCategory.JUMP_VELOCITY), 0.75)
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
