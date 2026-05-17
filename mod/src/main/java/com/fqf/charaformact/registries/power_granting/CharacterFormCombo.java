package com.fqf.charaformact.registries.power_granting;

public record CharacterFormCombo(ParsedCharacter character, ParsedForm form) {
	@Override
	public String toString() {
		return "CharacterFormCombo[" + character.ID + " in " + form.ID + "]";
	}
}
