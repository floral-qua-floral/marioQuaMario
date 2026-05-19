package com.fqf.charaformact.model;

import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.model.CommonSidedCharaFormModelDefinition;
import net.minecraft.util.Identifier;

/**
 * Represents the server's knowledge of a Character Form model. Very minimal, because the server only needs to know that
 * a model exists, as well as its stride length for the purpose of footstep calculations.
 */
public class ParsedCommonCharaFormModel {
	public final Identifier ID;
	public final ParsedCharacter CHARACTER;
	public final ParsedForm FORM;

	public final float STRIDE_LENGTH;

	public ParsedCommonCharaFormModel(CommonSidedCharaFormModelDefinition definition, ParsedCharacter character, ParsedForm form) {
		this.ID = definition.getCharacterID();
		this.CHARACTER = character;
		this.FORM = form;

		this.STRIDE_LENGTH = definition.getStrideLength();
	}
}
