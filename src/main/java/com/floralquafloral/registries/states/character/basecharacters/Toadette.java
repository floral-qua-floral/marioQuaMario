package com.floralquafloral.registries.states.character.basecharacters;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.character.CharacterDefinition;
import com.floralquafloral.stats.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class Toadette implements CharacterDefinition {
	@Override
	public void populateStatModifiers(Map<Set<StatCategory>, Double> modifiers) {

	}

	@Override
	public float getWidthFactor() {
		return 1.0F;
	}

	@Override
	public float getHeightFactor() {
		return 1.0F;
	}

	@Override
	public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "toadette");
	}

	@Override
	public void clientTick(MarioPlayerData data, boolean isSelf) {

	}

	@Override
	public void serverTick(MarioPlayerData data) {

	}
}
