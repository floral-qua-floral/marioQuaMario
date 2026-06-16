package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.mario_qua_mario.util.CharacterCustomizationUtil;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class CustomizableToadAppearanceModel extends ToadAppearanceModel {
	public CustomizableToadAppearanceModel(ModelPart root) {
		super(root);
	}

	@Override
	public void preActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		this.rightPigtail.visible = ((CharacterCustomizationUtil.CustomizablePlayerEntity) player).mqm$getCustomizationData(CharacterCustomizationUtil.HAS_PIGTAILS);
		this.leftPigtail.visible = this.rightPigtail.visible;

		super.preActionAnimation(player, data);
	}
}
