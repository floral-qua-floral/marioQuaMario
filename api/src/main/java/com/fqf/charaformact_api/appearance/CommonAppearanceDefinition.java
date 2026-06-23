package com.fqf.charaformact_api.appearance;

import org.joml.Vector3i;

public interface CommonAppearanceDefinition {
	default Vector3i getLegSize() {
		return new Vector3i(4, 12, 4);
	}
	default Vector3i getArmSize() {
		return new Vector3i(4, 12, 4);
	}
	default float getArmLength() {
		return this.getArmSize().y / 12F;
	}
	default float getStrideLength() {
		return 0.5F + this.getLegSize().y / 24F;
	}
}
