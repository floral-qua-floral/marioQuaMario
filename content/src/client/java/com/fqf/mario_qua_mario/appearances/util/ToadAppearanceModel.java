package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.mario_qua_mario.appearances.toads.SuperToadClientAppearance;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class ToadAppearanceModel extends MqmAppearanceModel {
	public final ModelPart capSpots;
	public final ModelPart rightPigtail;
	public final ModelPart leftPigtail;
	private final List<ModelPart> pigtailHatParts;

	public ToadAppearanceModel(Identifier id, ModelPart root) {
		super(id, root);

		ModelPart capBulb = this.head.getChild(SuperToadClientAppearance.CAP_BULB);
		this.capSpots = capBulb.getChild(SuperToadClientAppearance.CAP_HAT);
		this.rightPigtail = capBulb.getChild(SuperToadClientAppearance.RIGHT_PIGTAIL_TOP);
		this.leftPigtail = capBulb.getChild(SuperToadClientAppearance.LEFT_PIGTAIL_TOP);

		this.pigtailHatParts = List.of(
				this.rightPigtail.getChild(SuperToadClientAppearance.RIGHT_PIGTAIL_TOP_SPOTS),
				this.leftPigtail.getChild(SuperToadClientAppearance.LEFT_PIGTAIL_TOP_SPOTS)
		);
	}

	@Override
	public void postActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		super.postActionAnimation(player, data);

		boolean updatedHatVisibility = this.hat.visible != this.capSpots.visible;
		this.capSpots.visible = this.hat.visible;

		if(this.rightPigtail.visible || this.leftPigtail.visible) {
			this.rightPigtail.pitch = -this.head.pitch + this.rightPigtail.getDefaultTransform().pitch;

			// Animate the pigtails like a cape??

			this.leftPigtail.copyTransform(this.rightPigtail);
			this.leftPigtail.pivotX *= -1;

			if(updatedHatVisibility) for(ModelPart pigtailHatPart : this.pigtailHatParts) {
				pigtailHatPart.visible = this.hat.visible;
			}
		}
	}
}
