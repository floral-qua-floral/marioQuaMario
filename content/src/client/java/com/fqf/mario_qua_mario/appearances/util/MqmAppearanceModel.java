package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.MathHelper;

public abstract class MqmAppearanceModel extends AppearanceModel {
	public MqmAppearanceModel(ModelPart root) {
		super(root);
	}

	@Override
	public void setAngles(
			AbstractClientPlayerEntity player,
			float limbAngle, float limbDistance, float animationProgress,
			float headYaw, float headPitch
	) {
		super.setAngles(player, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

		if(this.tail != null) {
//			this.tail.pitch = 45;
		}
	}
}
