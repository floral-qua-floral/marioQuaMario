package com.fqf.charaformact_api;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.charaformact_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.util.AppearanceMapBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;

public interface CharaFormActAddon {
	void accumulateCharacters(ImmutableMap.Builder<Identifier, CharacterDefinition> builder);
	void accumulateForms(ImmutableMap.Builder<Identifier, FormDefinition> builder);
	void accumulateActionDefinitions(ImmutableMap.Builder<Identifier, IncompleteActionDefinition> builder);

	default void accumulateVoicelines(ImmutableSet.Builder<Identifier> builder) {

	}
	default void accumulateTransitionInjectionDefinitions(ImmutableList.Builder<TransitionInjectionDefinition> builder) {

	}
	default void accumulateCollisionAttackDefinitions(ImmutableMap.Builder<Identifier, CollisionAttackTypeDefinition> builder) {

	}

	default void accumulateCommonAppearances(AppearanceMapBuilder<CommonAppearanceDefinition> builder) {

	}
}
