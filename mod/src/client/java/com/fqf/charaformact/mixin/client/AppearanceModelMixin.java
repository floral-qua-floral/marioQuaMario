package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AppearanceModel.class)
public abstract class AppearanceModelMixin extends PlayerEntityModel<AbstractClientPlayerEntity> {
	// A mixin on my own class... Have I gone too far??
	public AppearanceModelMixin(ModelPart root, boolean thinArms) {
		super(root, thinArms);
	}

	@Override
	public void renderCape(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
		this.body.rotate(matrices);
		super.renderCape(matrices, vertices, light, overlay);
	}
}
