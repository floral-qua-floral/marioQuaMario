package com.fqf.charaformact_api.model;

import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class CharacterFormEntityModel extends PlayerEntityModel<AbstractClientPlayerEntity> {
	public final @Nullable ModelPart tail;
	public final @Nullable ModelPart rightEar;
	public final @Nullable ModelPart leftEar;
	public final @Nullable ModelPart rightWing;
	public final @Nullable ModelPart leftWing;

	public CharacterFormEntityModel(ModelPart root) {
		super(root, false);

		this.tail = this.getOptionalModelPart(this.getTailParent(root), EntityModelPartNames.TAIL);

		ModelPart earsParent = this.getEarsParent(root);
		this.rightEar = this.getOptionalModelPart(earsParent, EntityModelPartNames.RIGHT_EAR);
		this.leftEar = this.getOptionalModelPart(earsParent, EntityModelPartNames.LEFT_EAR);

		ModelPart wingsParent = this.getWingsParent(root);
		this.rightWing = this.getOptionalModelPart(wingsParent, EntityModelPartNames.RIGHT_WING);
		this.leftWing = this.getOptionalModelPart(wingsParent, EntityModelPartNames.LEFT_WING);

		this.head.xScale = 2;

		// Plan:
		// See if I can achieve universal armor fitting!
		// I might be able to get away with it if I use two mixins.
		// First: Jump in around line 130 of LivingEntityRenderer.
		//  Move the player's parts around! Specifically move their pivots & muck up their scale.
		// Second: Jump in around line 136.
		//  Reset everything!
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

	@Override
	public void setAngles(AbstractClientPlayerEntity livingEntity, float f, float g, float h, float i, float j) {
		super.setAngles(livingEntity, f, g, h, i, j);
//		this.head.yScale = 3;
//		this.rightArm.xScale = 4;
//		this.body.zScale = 3;
	}
}
