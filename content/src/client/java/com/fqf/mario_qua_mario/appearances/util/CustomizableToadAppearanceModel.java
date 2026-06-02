package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class CustomizableToadAppearanceModel extends ToadAppearanceModel {
	public CustomizableToadAppearanceModel(ModelPart root) {
		super(root);
	}

	@Override
	public void preActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		this.rightPigtail.visible = false;
		this.leftPigtail.visible = false;

		super.preActionAnimation(player, data);
	}
}
