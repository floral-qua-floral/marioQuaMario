package com.floralquafloral.registries.powerup.basepowerups;

import com.floralquafloral.CharaStat;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.powerup.PowerUpDefinition;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class SuperForm implements PowerUpDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "super");
	}

	@Override
	public void selfTick(MarioClientData data) {

	}

	@Override
	public void otherClientsTick(MarioPlayerData data) {

	}

	@Override
	public void serverTick(MarioPlayerData data) {

	}

	@Override public float getWidthFactor() {
		return 1.0F;
	}
	@Override public float getHeightFactor() {
		return 1.0F;
	}

	@Override public void populateStatFactors(EnumMap<CharaStat, Double> statFactorMap) {

	}

	// POWER-UP DATA
	@Override
	public void acquirePower(MarioData data) {

	}

	@Override
	public void losePower(MarioData data) {

	}

	@Override
	public int getValue() {
		return 1;
	}

	@Override
	public @Nullable SoundEvent getAcquisitionSound() {
		return null;
	}

	@Override
	public @Nullable Identifier getRevertTarget() {
		return Identifier.of("qua_mario:small");
	}

	@Override
	public @NotNull PowerHeart getHeart() {
		return new PowerHeart(
				Identifier.ofVanilla("hud/heart/full"),
				Identifier.ofVanilla("hud/heart/full_blinking"),
				Identifier.ofVanilla("hud/heart/half"),
				Identifier.ofVanilla("hud/heart/half_blinking")
		);
	}

	@Override
	public @Nullable PowerHeart getHeartHardcore() {
		return new PowerHeart(
				Identifier.ofVanilla("hud/heart/hardcore_full"),
				Identifier.ofVanilla("hud/heart/hardcore_full_blinking"),
				Identifier.ofVanilla("hud/heart/hardcore_half"),
				Identifier.ofVanilla("hud/heart/hardcore_half_blinking")
		);
	}

	@Override
	public @Nullable PowerHeartEmpty getHeartEmpty() {
		return null;
	}
}
