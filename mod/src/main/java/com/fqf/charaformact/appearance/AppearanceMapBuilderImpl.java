package com.fqf.charaformact.appearance;

import com.fqf.charaformact.registries.power_granting.AppearanceKey;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.charaformact_api.util.AppearanceMapBuilder;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AppearanceMapBuilderImpl<DefinitionType extends CommonAppearanceDefinition> implements AppearanceMapBuilder<DefinitionType> {
	private final ImmutableMap.Builder<AppearanceKey.Registerable, DefinitionType> BUILDER;

	public AppearanceMapBuilderImpl() {
		this.BUILDER = ImmutableMap.builder();
	}

	@Override
	public AppearanceMapBuilder<DefinitionType> put(Identifier appearanceID, Identifier characterID, Identifier formID, DefinitionType appearance) {
		this.BUILDER.put(new AppearanceKey.Registerable(appearanceID, characterID, formID), appearance);
		return this;
	}

	@Override
	public AppearanceMapBuilder<DefinitionType> putMatching(Identifier characterID, Identifier formID, DefinitionType appearance) {
		if(!characterID.getNamespace().equals(formID.getNamespace()))
			throw new IllegalArgumentException("AppearanceMapBuilder.putMatching must receive two IDs with matching namespaces!");
		Identifier appearanceID = Identifier.of(characterID.getNamespace(), formID.getPath() + "_" + characterID.getPath());
		return this.put(appearanceID, characterID, formID, appearance);
	}

	public Map<AppearanceKey.Registerable, DefinitionType> build() {
		return this.BUILDER.buildOrThrow();
	}
}
