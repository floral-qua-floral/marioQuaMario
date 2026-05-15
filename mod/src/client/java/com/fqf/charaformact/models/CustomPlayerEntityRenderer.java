package com.fqf.charaformact.models;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;

public class CustomPlayerEntityRenderer extends PlayerEntityRenderer {
	private final Identifier TEXTURE;

	public CustomPlayerEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, false);
		this.TEXTURE = Identifier.ofVanilla("textures/entity/goat/goat.png");
	}

	@Override
	public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
		return this.TEXTURE;
	}
}
