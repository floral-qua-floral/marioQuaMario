package com.fqf.charaformact_api.model;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface CommonSidedCharaFormModelDefinition {
	@NotNull Identifier getID();

	@NotNull Identifier getCharacterID();

	@NotNull Identifier getFormID();

	// Affects limb swing while walking, view bobbing, and the frequency of footstep sounds. This does not affect the
	// maximum amplitude of limb swing or view bob, only how much speed is required to reach that maximum amplitude.
	// This must be common-sided because of mechanics like Sculk Sensors that react to footsteps.
	float getStrideLength();
}
