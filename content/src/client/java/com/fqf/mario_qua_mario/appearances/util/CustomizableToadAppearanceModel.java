package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.mario_qua_mario.customization.CharacterCustomizationUtil;
import com.fqf.mario_qua_mario.customization.CustomizablePlayerEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

public class CustomizableToadAppearanceModel extends ToadAppearanceModel {
	public CustomizableToadAppearanceModel(Identifier id, ModelPart root) {
		super(id, root);
	}

	@Override
	public void preActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		this.rightPigtail.visible = ((CustomizablePlayerEntity) player).mqm$getCustomizationData(CharacterCustomizationUtil.HAS_PIGTAILS);
		this.leftPigtail.visible = this.rightPigtail.visible;

		super.preActionAnimation(player, data);
	}
}
