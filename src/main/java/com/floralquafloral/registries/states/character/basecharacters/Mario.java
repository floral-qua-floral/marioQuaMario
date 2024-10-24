package com.floralquafloral.registries.states.character.basecharacters;

import com.floralquafloral.stats.BaseStats;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.character.CharacterDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public class Mario implements CharacterDefinition {
	@Override
	public void populateStatFactors(EnumMap<BaseStats, Double> statFactorMap) {

	}

	@Override
	public float getWidthFactor() {
		return 0;
	}

	@Override
	public float getHeightFactor() {
		return 0;
	}

	@Override
	public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "mario");
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
}
