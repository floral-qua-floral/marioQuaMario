package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
	@Inject(method = "addFeature", at = @At("HEAD"))
	private void hewwo(FeatureRenderer<T, M> feature, CallbackInfoReturnable<Boolean> cir) {
		if((LivingEntityRenderer<T, M>) (Object) this instanceof PlayerEntityRenderer) {
			CharaFormAct.LOGGER.info("addFeature!\n\tI am: {}\n\tFeature is: {}", this, feature);
			CharaFormAct.LOGGER.info("...");
			CharaFormAct.LOGGER.info("um.....");
		}
	}
}
