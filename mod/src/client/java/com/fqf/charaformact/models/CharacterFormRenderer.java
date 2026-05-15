package com.fqf.charaformact.models;

import com.fqf.charaformact.CharaFormAct;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;

public class CharacterFormRenderer extends PlayerEntityRenderer {
	private final Identifier TEXTURE;

	public CharacterFormRenderer(EntityRendererFactory.Context ctx, Identifier textureLocation) {
		super(ctx, false);
		this.TEXTURE = textureLocation;
	}

	@Override
	public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
		return this.TEXTURE;
	}
}
