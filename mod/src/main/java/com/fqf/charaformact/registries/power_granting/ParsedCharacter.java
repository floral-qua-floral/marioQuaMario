package com.fqf.charaformact.registries.power_granting;

import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact_api.cfadata.CfaData;
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

		this.INITIAL_ACTION = Objects.requireNonNull(RegistryManager.ACTIONS.get(definition.defineInitialAction()),
				definition.defineID() + "'s initial action (" + definition.defineInitialAction() + ") doesn't exist!");
		this.INITIAL_FORM = Objects.requireNonNull(RegistryManager.FORMS.get(definition.defineInitialForm()),
				definition.defineID() + "'s initial form (" + definition.defineInitialForm() + ") doesn't exist!");

		this.JUMP_SOUND = definition.defineJumpSound();
		this.VOICE_NAME = definition.defineVoiceName();

		this.MODELS = new HashMap<>();
	}

	public AbstractParsedAction getMountedAction(CfaData data, Entity mount) {
		return RegistryManager.ACTIONS.get(this.CHARACTER_DEFINITION.chooseMountedAction(data, mount));
	}

	public float modifyIncomingDamage(CfaAuthoritativeData data, DamageSource source, float amount) {
		return this.CHARACTER_DEFINITION.modifyIncomingDamage(data, source, amount);
	}

	public AbstractParsedAction getInitialAction(CfaPlayerData data) {
		if(data.getPlayer().getVehicle() != null)
			return this.getMountedAction(data, data.getPlayer().getVehicle());
		return this.INITIAL_ACTION;
	}
}
