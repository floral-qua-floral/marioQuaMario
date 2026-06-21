package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.power_granting.CharacterFormCombo;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AbstractAppearanceCollector<DefinitionType extends CommonAppearanceDefinition, ParsedType> {
	protected Map<CharacterFormCombo, ParsedType> map;

	protected AbstractAppearanceCollector() {

	}

	// TODO: Move this system away from Entrypoints too
//	protected abstract Map<CharacterFormCombo, DefinitionType> getDefinitions();

	protected abstract String getEntrypoint();
	protected abstract Class<DefinitionType> getEntrypointClass();

	protected abstract ParsedType parse(DefinitionType definition, ParsedCharacter character, ParsedForm form);

	public void collect() {
		ImmutableMap.Builder<CharacterFormCombo, ParsedType> builder = ImmutableMap.builder();

		for(DefinitionType definition : RegistryManager.getEntrypoints(this.getEntrypoint(), this.getEntrypointClass())) {
			Identifier modelID = definition.getID();
			Identifier characterID = definition.getCharacterID();
			Identifier formID = definition.getFormID();
			ParsedCharacter character = RegistryManager.CHARACTERS.get(characterID);
			ParsedForm form = RegistryManager.FORMS.get(formID);

			if(character == null && form == null)
				CharaFormAct.LOGGER.error("Model {}'s character ({}) and form ({}) are both unregistered?? Ignoring...",
						modelID, characterID, formID);
			else if(character == null)
				CharaFormAct.LOGGER.warn("Model {}'s character ({}) is unregistered, ignoring...", modelID, characterID);
			else if(form == null)
				CharaFormAct.LOGGER.warn("Model {}'s form ({}) is unregistered, ignoring...", modelID, formID);
			else
				builder.put(new CharacterFormCombo(character, form), this.parse(definition, character, form));
		}

		this.map = builder.build();
	}

	public @Nullable ParsedType get(CharacterFormCombo combo) {
		return this.map.get(combo);
	}
}
