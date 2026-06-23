package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.power_granting.AppearanceKey;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AbstractAppearanceCollector<DefinitionType extends CommonAppearanceDefinition, ParsedType> {
	protected Map<AppearanceKey, ParsedType> map;
	protected Map<Identifier, Pair<AppearanceKey, ParsedCommonAppearance>> validationMap;

	protected AbstractAppearanceCollector() {

	}

	protected abstract Map<AppearanceKey.Registerable, DefinitionType> getDefinitions();

	protected abstract ParsedType parse(AppearanceKey.Registerable key, DefinitionType definition);

	protected abstract ParsedCommonAppearance refine(ParsedType from);

	public void collect() {
		Map<AppearanceKey.Registerable, DefinitionType> definitions = this.getDefinitions();

		ImmutableMap.Builder<AppearanceKey, ParsedType> builder = ImmutableMap.builderWithExpectedSize(definitions.size());
		ImmutableMap.Builder<Identifier, Pair<AppearanceKey, ParsedCommonAppearance>> validationBuilder = ImmutableMap.builderWithExpectedSize(definitions.size());

		definitions.forEach((key, definition) -> {
			Identifier modelID = key.ID;
			Identifier characterID = key.CHARACTER;
			Identifier formID = key.FORM;

			ParsedCharacter character = RegistryManager.CHARACTERS.get(characterID);
			ParsedForm form = RegistryManager.FORMS.get(formID);

			if(character == null && form == null)
				CharaFormAct.LOGGER.error("Model {}'s character ({}) and form ({}) are both unregistered?? Ignoring...",
						modelID, characterID, formID);
			else if(character == null)
				CharaFormAct.LOGGER.warn("Model {}'s character ({}) is unregistered, ignoring...", modelID, characterID);
			else if(form == null)
				CharaFormAct.LOGGER.warn("Model {}'s form ({}) is unregistered, ignoring...", modelID, formID);
			else {
				ParsedType parsed = this.parse(key, definition);
				builder.put(key, parsed);
				validationBuilder.put(key.ID, new Pair<>(key, this.refine(parsed)));
			}
		});

		this.map = builder.build();
		this.validationMap = validationBuilder.build();
	}

	public @Nullable ParsedType get(AppearanceKey combo) {
		return this.map.get(combo);
	}
}
