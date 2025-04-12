package com.fqf.mario_qua_mario_content.entity;

import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.entity.custom.MarioFireballProjectileEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class MarioFireballModel extends EntityModel<MarioFireballProjectileEntity> {
	public static final EntityModelLayer FIREBALL = new EntityModelLayer(MarioQuaMarioContent.makeID("mario_fireball"), "main");
	private final ModelPart fireball;

	public MarioFireballModel(ModelPart root) {
		this.fireball = root.getChild("fireball");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData fireball = modelPartData.addChild("fireball", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 21.0F, 0.0F));

		ModelPartData tail4_r1 = fireball.addChild("tail4_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0F, 3.0F, -1.3963F, 0.0F, 0.0F));

		ModelPartData tail3_r1 = fireball.addChild("tail3_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 3.0F, 3.0F, -2.9671F, 0.0F, 0.0F));

		ModelPartData tail2_r1 = fireball.addChild("tail2_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 3.0F, -3.0F, 1.7453F, 0.0F, 0.0F));

		ModelPartData tail1_r1 = fireball.addChild("tail1_r1", ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, 0.0F, 0.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0F, -3.0F, 0.1745F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 24, 18);
	}

	@Override
	public void setAngles(MarioFireballProjectileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
//		this.fireball.pitch = 45;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		fireball.render(matrices, vertexConsumer, light, overlay, color);
	}
}
