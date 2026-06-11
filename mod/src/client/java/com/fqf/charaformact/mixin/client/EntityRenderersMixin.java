package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {
	// Set up all the Appearance Renderers first, so that they'll be ready to receive captured Feature Renderers
	@Inject(method = "reloadPlayerRenderers", at = @At("HEAD"))
	private static void addCfaPlayermodels(EntityRendererFactory.Context ctx, CallbackInfoReturnable<Map<SkinTextures.Model, EntityRenderer<? extends PlayerEntity>>> cir) {
		ClientAppearanceCollector.INSTANCE.reloadAppearanceRenderers(ctx);
	}
}
