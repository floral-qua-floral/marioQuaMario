package com.fqf.charaformact.appearance.temp;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class WonkyCommonAppearance implements CommonAppearanceDefinition {
	@Override public @NotNull Identifier getID() {
		return CharaFormAct.makeID("wonky");
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Identifier.of("mario_qua_mario", "mario");
	}
	@Override public @NotNull Identifier getFormID() {
		return Identifier.of("mario_qua_mario", "raccoon");
	}

	@Override public Vector3i getArmSize() {
		return new Vector3i(6, 12*3, 2);
	}
	@Override public Vector3i getLegSize() {
		return new Vector3i(2, 36, 2);
	}
}
