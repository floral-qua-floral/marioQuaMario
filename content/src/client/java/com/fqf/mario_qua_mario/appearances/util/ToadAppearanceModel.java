package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.mario_qua_mario.appearances.toads.AbstractToadClientAppearance;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class ToadAppearanceModel extends MqmAppearanceModel {
	public final ModelPart capSpots;
	public final ModelPart rightPigtail;
	public final ModelPart leftPigtail;

	public ToadAppearanceModel(ModelPart root) {
		super(root);

		ModelPart capBulb = this.head.getChild(AbstractToadClientAppearance.CAP_BULB);
		this.capSpots = capBulb.getChild(AbstractToadClientAppearance.CAP_HAT);
		this.rightPigtail = capBulb.getChild(AbstractToadClientAppearance.RIGHT_PIGTAIL_TOP);
		this.leftPigtail = capBulb.getChild(AbstractToadClientAppearance.LEFT_PIGTAIL_TOP);
	}

	@Override
	public void postActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		super.postActionAnimation(player, data);

		this.capSpots.visible = this.hat.visible;

		if(this.rightPigtail.visible || this.leftPigtail.visible) {
			this.rightPigtail.pitch = -this.head.pitch + this.rightPigtail.getDefaultTransform().pitch;

			// Animate the pigtails like a cape??

			this.leftPigtail.copyTransform(this.rightPigtail);
			this.leftPigtail.pivotX *= -1;
		}
	}
}
