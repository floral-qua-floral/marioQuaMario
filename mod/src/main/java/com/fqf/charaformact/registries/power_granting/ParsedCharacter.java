package com.fqf.charaformact.registries.power_granting;

import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ParsedCharacter extends ParsedPowerGrantingState {
	private final CharacterDefinition CHARACTER_DEFINITION;
	public final AbstractParsedAction INITIAL_ACTION;
	public final ParsedForm INITIAL_FORM;
	public final SoundEvent JUMP_SOUND;
	public final String VOICE_NAME;

	public final Map<ParsedForm, String> MODELS;

	public ParsedCharacter(CharacterDefinition definition) {
		super(definition);
		this.CHARACTER_DEFINITION = definition;

		this.INITIAL_ACTION = Objects.requireNonNull(RegistryManager.ACTIONS.get(definition.getInitialAction()),
				definition.getID() + "'s initial action (" + definition.getInitialAction() + ") doesn't exist!");
		this.INITIAL_FORM = Objects.requireNonNull(RegistryManager.FORMS.get(definition.getInitialForm()),
				definition.getID() + "'s initial form (" + definition.getInitialForm() + ") doesn't exist!");

		this.JUMP_SOUND = definition.getJumpSound();
		this.VOICE_NAME = definition.getVoiceName();

		this.MODELS = new HashMap<>();
	}

	public AbstractParsedAction getMountedAction(Entity mount) {
		return RegistryManager.ACTIONS.get(this.CHARACTER_DEFINITION.getMountedAction(mount));
	}

	public float modifyIncomingDamage(CfaAuthoritativeData data, DamageSource source, float amount) {
		return this.CHARACTER_DEFINITION.modifyIncomingDamage(data, source, amount);
	}

	public AbstractParsedAction getInitialAction(CfaPlayerData data) {
		if(data.getPlayer().getVehicle() != null)
			return this.getMountedAction(data.getPlayer().getVehicle());
		return this.INITIAL_ACTION;
	}
}
