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

public class Super implements PowerUpDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("super");
	}

	@Override public @Nullable Identifier getReversionTarget() {
		return MarioQuaMarioContent.makeID("small");
	}
	@Override public int getValue() {
		return 1;
	}

	@Override public @Nullable SoundEvent getAcquisitionSound() {
		return null;
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}

	@Override public float getVoicePitch() {
		return 1;
	}

	@Override public Set<String> getPowers() {
		return Set.of();
	}

	@Override public @NotNull PowerHeart getPowerHeart(PowerHeartHelper helper) {
		return new PowerHeart(
				Identifier.ofVanilla("hud/heart/full"),
				Identifier.ofVanilla("hud/heart/full_blinking"),
				Identifier.ofVanilla("hud/heart/half"),
				Identifier.ofVanilla("hud/heart/half_blinking"),
				Identifier.ofVanilla("hud/heart/hardcore_full"),
				Identifier.ofVanilla("hud/heart/hardcore_full_blinking"),
				Identifier.ofVanilla("hud/heart/hardcore_half"),
				Identifier.ofVanilla("hud/heart/hardcore_half_blinking"),
				Identifier.ofVanilla("hud/heart/container"),
				Identifier.ofVanilla("hud/heart/container_blinking")
		);
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of();
	}
}
