package com.floralquafloral.registries.states.powerup.basepowerups;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.definitions.PowerUpDefinition;
import com.floralquafloral.definitions.actions.StatCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmallForm implements PowerUpDefinition {
	@Override public @NotNull Map<String, String> getCharacterPlayermodels() {
		return Map.of();
	}

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "small");
	}

	@Override
	public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override
	public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public int getBumpStrengthModifier() {
		return -1;
	}
	@Override public float getWidthFactor() {
		return 1.0F;
	}
	@Override public float getHeightFactor() {
		return 0.5F;
	}

	@Override public void populateStatModifiers(Map<Set<StatCategory>, Double> modifiers) {

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
	public float getVoicePitch() {
		return 1.075F;
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

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}
