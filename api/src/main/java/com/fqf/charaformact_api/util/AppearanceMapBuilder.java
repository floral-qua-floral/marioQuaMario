package com.fqf.charaformact_api.util;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import net.minecraft.util.Identifier;

public interface AppearanceMapBuilder<T extends CommonAppearanceDefinition> {
	AppearanceMapBuilder<T> put(Identifier characterID, Identifier formID, T appearance);
}
