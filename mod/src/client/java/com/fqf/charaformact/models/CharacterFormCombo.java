package com.fqf.charaformact.models;

public record CharacterFormCombo(int numberoo) {


	public String getName() {
		return "placeholder " + this.numberoo;
	}
}
