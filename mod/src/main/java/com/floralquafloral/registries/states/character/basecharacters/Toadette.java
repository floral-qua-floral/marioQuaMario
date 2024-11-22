package com.floralquafloral.registries.states.character.basecharacters;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.definitions.CharacterDefinition;
import com.floralquafloral.definitions.actions.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class Toadette implements CharacterDefinition {
	@Override public @NotNull Map<String, String> getPoweredUpPlayermodels() {
		return Map.of(
				"qua_mario:small", "Small Toadette",
				"qua_mario:super", "Super Toadette"
		);
	}

	@Override
	public void populateStatModifiers(Map<Set<StatCategory>, Double> modifiers) {

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

	@Override
	public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "toadette");
	}

	@Override
	public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override
	public void serverTick(MarioAuthoritativeData data) {

	}
}
