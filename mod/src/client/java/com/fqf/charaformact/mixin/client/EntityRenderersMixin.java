package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.models.CfaPlayerModelHelper;
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
	@Inject(method = "reloadPlayerRenderers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
	private static void addCfaPlayermodels(EntityRendererFactory.Context ctx, CallbackInfoReturnable<Map<SkinTextures.Model, EntityRenderer<? extends PlayerEntity>>> cir) {
		CfaPlayerModelHelper.reloadCustomPlayerRenderers(ctx);
	}
}
