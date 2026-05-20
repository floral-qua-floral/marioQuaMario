package com.fqf.charaformact_api.appearance;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public interface CommonAppearanceDefinition {
	@NotNull Identifier getID();

	@NotNull Identifier getCharacterID();
	@NotNull Identifier getFormID();

	default Vector3i getLegSize() {
		return new Vector3i(4, 12, 4);
	}
	default Vector3i getArmSize() {
		return new Vector3i(4, 12, 4);
	}
	default float getStrideLength() {
		return 0.5F + this.getLegSize().y / 24F;
	}


}
