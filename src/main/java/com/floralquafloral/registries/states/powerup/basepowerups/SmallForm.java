package com.floralquafloral.registries.states.powerup.basepowerups;

import com.floralquafloral.CharaStat;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.powerup.PowerUpDefinition;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class SmallForm implements PowerUpDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "small");
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
		return 0.5F;
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
		return 0;
	}

	@Override
	public @Nullable SoundEvent getAcquisitionSound() {
		return null;
	}

	@Override
	public @Nullable Identifier getRevertTarget() {
		return null;
	}

	@Override
	public @NotNull PowerHeart getHeart() {
		return new PowerHeart(
				MarioQuaMario.MOD_ID,
				"small"
		);
	}

	@Override
	public @Nullable PowerHeart getHeartHardcore() {
		return new PowerHeart(
				MarioQuaMario.MOD_ID,
				"small/hardcore"
		);
	}

	@Override
	public @Nullable PowerHeartEmpty getHeartEmpty() {
		return new PowerHeartEmpty(
				MarioQuaMario.MOD_ID,
				"small"
		);
	}
}
