package com.floralquafloral.definitions;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface CharacterDefinition extends MarioMajorStateDefinition {
	@NotNull Map<String, String> getPoweredUpPlayermodels();
}
