package com.fqf.mario_qua_mario_content.powerups;

import com.fqf.mario_qua_mario_api.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Small implements PowerUpDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("small");
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
	@Override public float getAnimationWidthFactor() {
		return 1;
	}
	@Override public float getAnimationHeightFactor() {
		return 0.45F;
	}

	@Override public int getBumpStrengthModifier() {
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

	@Override public @NotNull PowerHeart getPowerHeart(PowerHeartHelper helper) {
		return new PowerUpDefinition.PowerHeart(
				MarioQuaMarioContent.makeResID("hud/power_hearts/small/full"),
				MarioQuaMarioContent.makeResID("hud/power_hearts/small/full_blinking"),

				MarioQuaMarioContent.makeResID("hud/power_hearts/small/half"),
				MarioQuaMarioContent.makeResID("hud/power_hearts/small/half_blinking"),

				MarioQuaMarioContent.makeResID("hud/power_hearts/small/hardcore/full"),
				MarioQuaMarioContent.makeResID("hud/power_hearts/small/hardcore/full_blinking"),

				MarioQuaMarioContent.makeResID("hud/power_hearts/small/hardcore/half"),
				MarioQuaMarioContent.makeResID("hud/power_hearts/small/hardcore/half_blinking"),

				MarioQuaMarioContent.makeResID("hud/power_hearts/small/container"),
				MarioQuaMarioContent.makeResID("hud/power_hearts/small/container_blinking")
		);
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
