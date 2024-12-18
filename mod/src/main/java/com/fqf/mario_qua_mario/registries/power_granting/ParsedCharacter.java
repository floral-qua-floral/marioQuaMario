package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.registries.ParsedMarioThing;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;

import java.util.Objects;

public class ParsedCharacter extends ParsedPowerGrantingState {
	public final AbstractParsedAction INITIAL_ACTION;
	public final ParsedPowerUp INITIAL_POWER_UP;

	public ParsedCharacter(CharacterDefinition definition) {
		super(definition);

		this.INITIAL_ACTION = Objects.requireNonNull(RegistryManager.ACTIONS.get(definition.getInitialAction()),
				definition.getID() + "'s initial action (" + definition.getInitialAction() + ") doesn't exist!");
		this.INITIAL_POWER_UP = Objects.requireNonNull(RegistryManager.POWER_UPS.get(definition.getInitialPowerUp()),
				definition.getID() + "'s initial power-up (" + definition.getInitialPowerUp() + ") doesn't exist!");

	}
}
