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

import java.util.Map;
import java.util.Set;

public class SuperForm implements PowerUpDefinition {
	@Override public @NotNull Map<String, String> getCharacterPlayermodels() {
		return Map.of();
	}

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "super");
	}

	@Override
	public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override
	public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}
	@Override public float getWidthFactor() {
		return 1.0F;
	}
	@Override public float getHeightFactor() {
		return 1.0F;
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
		return 1;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
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
