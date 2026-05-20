package com.fqf.charaformact.appearance.temp;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TemplateCommonAppearance implements CommonAppearanceDefinition {
	@Override public @NotNull Identifier getID() {
		return CharaFormAct.makeID("template");
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Identifier.of("mario_qua_mario", "mario");
	}
	@Override public @NotNull Identifier getFormID() {
		return Identifier.of("mario_qua_mario", "super");
	}
}
