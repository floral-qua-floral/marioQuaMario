package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.Identifier;

public class PlumberAppearanceModel extends MqmAppearanceModel {
	public final ModelPart capfulHead;
	public final ModelPart caplessHead;
	public final ModelPart capfulNose;
	public final ModelPart caplessNose;
	public final ModelPart caplessRightEar;
	public final ModelPart caplessLeftEar;

	public PlumberAppearanceModel(Identifier id, ModelPart root) {
		super(id, root);

		this.capfulHead = this.head.getChild(PlumberClientAppearance.CAPFUL_HEAD);
		this.caplessHead = this.head.getChild(PlumberClientAppearance.CAPLESS_HEAD);
		this.capfulNose = this.capfulHead.getChild(EntityModelPartNames.NOSE);
		this.caplessNose = this.caplessHead.getChild(EntityModelPartNames.NOSE);
		this.caplessHead.visible = false;

		if(this.rightEar != null && this.leftEar != null) {
			this.caplessRightEar = this.caplessHead.getChild(EntityModelPartNames.RIGHT_EAR);
			this.caplessLeftEar = this.caplessHead.getChild(EntityModelPartNames.LEFT_EAR);
		}
		else {
			this.caplessRightEar = null;
			this.caplessLeftEar = null;
		}
	}

	@Override
	protected ModelPart getEarsParent(ModelPart root) {
		return this.head.getChild(PlumberClientAppearance.CAPFUL_HEAD);
	}

	@Override
	public void preActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		boolean hideCap = data.isCovered(EquipmentCoverSpot.HEADGEAR);
		this.capfulHead.visible = !hideCap;
		this.caplessHead.visible = hideCap;

		this.capfulNose.zScale = data.isCovered(EquipmentCoverSpot.NOSE) ? 0.43F : 1;

		this.caplessNose.copyTransform(this.capfulNose);

		if(this.rightEar != null && this.leftEar != null) {
			this.caplessRightEar.copyTransform(this.rightEar);
			this.caplessLeftEar.copyTransform(this.leftEar);
		}

		super.preActionAnimation(player, data);
	}
}
