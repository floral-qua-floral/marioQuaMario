package com.floralquafloral.registries.states.character;

import com.floralquafloral.definitions.CharacterDefinition;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.states.ParsedMajorMarioState;
import com.floralquafloral.definitions.actions.CharaStat;

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
