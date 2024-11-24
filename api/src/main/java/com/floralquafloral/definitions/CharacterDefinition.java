package com.floralquafloral.definitions;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface CharacterDefinition extends MarioStatAlteringStateDefinition {
	@NotNull Map<String, String> getPoweredUpPlayermodels();
}
