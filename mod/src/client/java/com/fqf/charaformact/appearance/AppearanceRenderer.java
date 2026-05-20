package com.fqf.charaformact.appearance;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class AppearanceRenderer extends PlayerEntityRenderer {
	private final Identifier TEXTURE;

	public AppearanceRenderer(EntityRendererFactory.Context ctx, Identifier textureLocation) {
		super(ctx, false);
		this.TEXTURE = textureLocation;

		this.disgustingIllegalAtrociouslyUpsettingHacksToAddCustomFeatureRenderers(ctx);
	}

	@SuppressWarnings({"UnstableApiUsage", "unchecked", "rawtypes"})
	private void disgustingIllegalAtrociouslyUpsettingHacksToAddCustomFeatureRenderers(EntityRendererFactory.Context context) {
		// This is really gross... But I don't know of a better way to make this work. It seems like entity renderer
		// registration (specifically the vanilla logic that Fabric API hooks into) is VERY tied to Entity Types, with
		// the exception of player renderers, which are instead equally closely tied to the SLIM | WIDE arm type enum.
		// CFA playermodels cannot be associated with a unique entity type, nor can they be associated with a unique
		// arm width enum... So this is the cleanest solution I could figure out. :/
		LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) this;
		LivingEntityFeatureRendererRegistrationCallback.EVENT.invoker()
				.registerRenderers(EntityType.PLAYER, this, new RegistrationHelperImpl(accessor::callAddFeature), context);
	}

	@Override
	public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
		return this.TEXTURE;
	}
}
