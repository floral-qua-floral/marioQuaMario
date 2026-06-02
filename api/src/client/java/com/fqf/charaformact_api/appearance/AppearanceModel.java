package com.fqf.charaformact_api.appearance;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class AppearanceModel extends PlayerEntityModel<AbstractClientPlayerEntity> {
	public final @Nullable ModelPart tail;
	public final @Nullable ModelPart rightEar;
	public final @Nullable ModelPart leftEar;
	public final @Nullable ModelPart rightWing;
	public final @Nullable ModelPart leftWing;

	public AppearanceModel(ModelPart root) {
		super(root, false);

		this.tail = this.getOptionalModelPart(this.getTailParent(root), EntityModelPartNames.TAIL);

		ModelPart earsParent = this.getEarsParent(root);
		this.rightEar = this.getOptionalModelPart(earsParent, EntityModelPartNames.RIGHT_EAR);
		this.leftEar = this.getOptionalModelPart(earsParent, EntityModelPartNames.LEFT_EAR);

		ModelPart wingsParent = this.getWingsParent(root);
		this.rightWing = this.getOptionalModelPart(wingsParent, EntityModelPartNames.RIGHT_WING);
		this.leftWing = this.getOptionalModelPart(wingsParent, EntityModelPartNames.LEFT_WING);

		this.head.xScale = 2;
	}

	protected ModelPart getTailParent(ModelPart root) {
		return this.body;
	}

	protected ModelPart getEarsParent(ModelPart root) {
		return this.head;
	}

	protected ModelPart getWingsParent(ModelPart root) {
		return this.body;
	}

	protected @Nullable ModelPart getOptionalModelPart(ModelPart root, String name) {
		try {
			return root.getChild(name);
		}
		catch(NoSuchElementException exception) {
			return null;
		}
	}

	@Override public final void setAngles(AbstractClientPlayerEntity livingEntity, float f, float g, float h, float i, float j) {
		super.setAngles(livingEntity, f, g, h, i, j);
	}

	public void preActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		// This method is called after vanilla has posed the model, but before CFA has applied Action animations.
	}

	public void postActionAnimation(AbstractClientPlayerEntity player, CfaAnimatingData data) {
		// This method is called after CFA has applied Action animations. Use this sparingly!
	}
}
