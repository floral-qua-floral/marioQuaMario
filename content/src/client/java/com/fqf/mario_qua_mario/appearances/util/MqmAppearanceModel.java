package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.util.Easing;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.util.math.MathHelper.HALF_PI;

public abstract class MqmAppearanceModel extends AppearanceModel {
	public MqmAppearanceModel(Identifier id, ModelPart root) {
		super(id, root);
	}

	@Override
	public void preActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		if(this.rightEar != null && this.leftEar != null) {
			int offset = data.isCovered(EquipmentCoverSpot.SCALP) ? 1 : 0;
			this.adjustEar(this.rightEar, offset);
			this.adjustEar(this.leftEar, -offset);
		}

		if(this.tail != null) {
			float swing = this.leftLeg.pitch - this.rightLeg.pitch;
			float lift;
			if(player.isOnGround() || data.getActionCategory() == ActionCategory.WALLBOUND) {
				lift = Easing.SINE_IN_OUT.ease(Easing.clampedRangeToProgress(data.getForwardVel(), 0, 0.55));
				swing += MathHelper.sin(data.getPlayer().age / 17F) * 0.5F * Math.max(0F, MathHelper.HALF_PI * 0.5F - Math.abs(swing));
			}
			else lift = Easing.EXPO_IN_OUT.ease(Easing.clampedRangeToProgress(data.getYVel(), 0.87, -0.85), 0.45F, 1.8F);

			float inverseLift = 1 - lift;

			this.tail.setAngles(
					-0.65F * inverseLift * HALF_PI,
					swing * -0.2028F,
					swing * 0.312F * inverseLift
			);
		}
	}

	private void adjustEar(ModelPart ear, int factor) {
		ear.setTransform(ear.getDefaultTransform());
		ear.pivotX += factor;
		ear.pivotY -= Math.abs(factor);
	}
}
