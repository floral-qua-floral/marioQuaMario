package com.fqf.mario_qua_mario.powerups;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Small implements PowerUpDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("small");
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

	@Override public int getBumpStrengthModifier() {
		return -1;
	}

	@Override public float getVoicePitch() {
		return 1.075F;
	}

	@Override public Set<String> getPowers() {
		return Set.of();
	}

	@Override public @NotNull PowerHeart getPowerHeart(PowerHeartHelper helper) {
		String namespace = "mario_qua_mario";

		return new PowerUpDefinition.PowerHeart(
				Identifier.of(namespace, "hud/power_hearts/small/full"),
				Identifier.of(namespace, "hud/power_hearts/small/full_blinking"),

				Identifier.of(namespace, "hud/power_hearts/small/half"),
				Identifier.of(namespace, "hud/power_hearts/small/half_blinking"),

				Identifier.of(namespace, "hud/power_hearts/small/hardcore/full"),
				Identifier.of(namespace, "hud/power_hearts/small/hardcore/full_blinking"),

				Identifier.of(namespace, "hud/power_hearts/small/hardcore/half"),
				Identifier.of(namespace, "hud/power_hearts/small/hardcore/half_blinking"),

				Identifier.of(namespace, "hud/power_hearts/small/container"),
				Identifier.of(namespace, "hud/power_hearts/small/container_blinking")
		);
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}

	@Override public @NotNull List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}
