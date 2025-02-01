package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.tom.cpm.shared.io.ModelFile;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ParsedCharacter extends ParsedPowerGrantingState {
	private final CharacterDefinition CHARACTER_DEFINITION;
	public final AbstractParsedAction INITIAL_ACTION;
	public final ParsedPowerUp INITIAL_POWER_UP;
	public final SoundEvent JUMP_SOUND;

	public final Identifier RESOURCE_ID;

	public final Map<ParsedPowerUp, ModelFile> MODELS;

	public ParsedCharacter(CharacterDefinition definition) {
		super(definition);
		this.CHARACTER_DEFINITION = definition;

		this.INITIAL_ACTION = Objects.requireNonNull(RegistryManager.ACTIONS.get(definition.getInitialAction()),
				definition.getID() + "'s initial action (" + definition.getInitialAction() + ") doesn't exist!");
		this.INITIAL_POWER_UP = Objects.requireNonNull(RegistryManager.POWER_UPS.get(definition.getInitialPowerUp()),
				definition.getID() + "'s initial power-up (" + definition.getInitialPowerUp() + ") doesn't exist!");

		this.JUMP_SOUND = definition.getJumpSound();

		this.RESOURCE_ID = this.ID.getNamespace().equals("mqm") ? MarioQuaMario.makeResID(this.ID.getPath()) : this.ID;
		this.MODELS = new HashMap<>();
	}

	public AbstractParsedAction getMountedAction(Entity mount) {
		return RegistryManager.ACTIONS.get(this.CHARACTER_DEFINITION.getMountedAction(mount));
	}
}
