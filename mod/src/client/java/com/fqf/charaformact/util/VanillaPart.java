package com.fqf.charaformact.util;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import net.minecraft.client.model.ModelPart;

import java.util.function.Function;

public enum VanillaPart {
	HEAD(false, true, model -> model.head),
	TORSO(true, true, model -> model.body),
	RIGHT_ARM(false, true, model -> model.rightArm),
	LEFT_ARM(false, true, model -> model.leftArm),
	RIGHT_LEG(true, false, model -> model.rightLeg),
	LEFT_LEG(true, false, model -> model.leftLeg);
	public final boolean HAS_INNER_ARMOR, HAS_SPECIAL;
	private final Function<AppearanceModel, ModelPart> finder;

	VanillaPart(boolean hasInnerArmor, boolean hasSpecial, Function<AppearanceModel, ModelPart> finder) {
		this.HAS_INNER_ARMOR = hasInnerArmor;
		this.HAS_SPECIAL = hasSpecial;
		this.finder = finder;
	}

	public ModelPart of(AppearanceModel model) {
		return this.finder.apply(model);
	}
}
