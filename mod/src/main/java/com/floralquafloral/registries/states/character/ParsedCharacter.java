package com.floralquafloral.registries.states.character;

import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.states.ParsedMajorMarioState;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;

import java.util.Map;
import java.util.Set;

public class ParsedCharacter extends ParsedMajorMarioState {
	public ParsedCharacter(CharacterDefinition definition) {
		super(definition);
	}

	@Override
	public double getStatMultiplier(CharaStat stat) {
		if(MarioDataManager.useCharacterStats) return super.getStatMultiplier(stat);
		else return 1.0;
	}
}
