package com.fqf.charaformact.appearance;

import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * Represents the server's knowledge of an Appearance. Very minimal, because the server only needs to know that
 * a model exists, as well as its stride length for the purpose of footstep calculations (for Sculk).
 */
public class ParsedCommonAppearance {
	public final Identifier ID;
	public final ParsedCharacter CHARACTER;
	public final ParsedForm FORM;

	public final float STRIDE_LENGTH;

	public ParsedCommonAppearance(CommonAppearanceDefinition definition, ParsedCharacter character, ParsedForm form) {
		this.ID = Objects.requireNonNull(definition.getID());
		this.CHARACTER = character;
		this.FORM = form;

		this.STRIDE_LENGTH = definition.getStrideLength();
	}
}
