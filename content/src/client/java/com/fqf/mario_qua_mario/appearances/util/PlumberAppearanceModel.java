package com.fqf.mario_qua_mario.appearances.util;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;

public class PlumberAppearanceModel extends MqmAppearanceModel {
	public final ModelPart capfulHead;
	public final ModelPart caplessHead;

	public PlumberAppearanceModel(ModelPart root) {
		super(root);

		this.capfulHead = this.head.getChild(PlumberClientAppearance.CAPFUL_HEAD);
		this.caplessHead = this.head.getChild(PlumberClientAppearance.CAPLESS_HEAD);
		this.caplessHead.visible = false;
	}

	@Override
	public void setAngles(AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		boolean showCap = player.getEquippedStack(EquipmentSlot.HEAD).isEmpty();
		this.capfulHead.visible = showCap;
		this.caplessHead.visible = !showCap;

		super.setAngles(player, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
	}
}
