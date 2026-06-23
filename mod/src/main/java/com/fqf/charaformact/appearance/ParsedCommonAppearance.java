package com.fqf.charaformact.appearance;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * Represents the server's knowledge of an Appearance. Very minimal, because the server only needs to know that
 * a model exists, as well as its stride length for the purpose of footstep calculations (for Sculk).
 */
public class ParsedCommonAppearance {
	public final Identifier ID;

	public final float STRIDE_LENGTH;
	public final float ARM_LENGTH;

	public ParsedCommonAppearance(Identifier id, CommonAppearanceDefinition definition) {
		this.ID = id;

		this.STRIDE_LENGTH = definition.getStrideLength();
		this.ARM_LENGTH = definition.getArmLength();
	}
}
