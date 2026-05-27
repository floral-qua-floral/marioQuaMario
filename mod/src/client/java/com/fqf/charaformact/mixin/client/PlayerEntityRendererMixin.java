package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
	@WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/model/ModelPart;Z)Lnet/minecraft/client/render/entity/model/PlayerEntityModel;"))
	private static PlayerEntityModel<AbstractClientPlayerEntity> uwu(ModelPart root, boolean thinArms, Operation<PlayerEntityModel<AbstractClientPlayerEntity>> original, @Local(argsOnly = true) EntityRendererFactory.Context ctx) {
		AppearanceModel currentCustomModel = ClientAppearanceCollector.INSTANCE.getCustomModelForRenderer();
		if(currentCustomModel != null) {
			CharaFormAct.LOGGER.info("Instantiating a custom player renderer! HOLY SMOKES!!!!");
//			return original.call(root, thinArms);
			return currentCustomModel;
		}
		else {
			CharaFormAct.LOGGER.info("Instantiating a vanilla player renderer, with {} arms!", thinArms ? "thin" : "wide");
			return original.call(root, thinArms);
		}
	}

	@Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At("TAIL"))
	private void applyEverythingMutator(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
		if(abstractClientPlayerEntity.cfa$getCfaData().isEnabled()) {
			abstractClientPlayerEntity.cfa$getAppearanceData().arrangeModel(matrixStack, tickDelta);
		}
//		abstractClientPlayerEntity.cfa$getAnimationData().rotateTotalPlayermodel(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true), abstractClientPlayerEntity, matrixStack);
	}

	@WrapOperation(method = "getPositionOffset(Lnet/minecraft/client/network/AbstractClientPlayerEntity;F)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isInSneakingPose()Z"))
	private boolean preventSneakingOffset(AbstractClientPlayerEntity instance, Operation<Boolean> original) {
		return original.call(instance) && !instance.cfa$getCfaData().isEnabled();
	}
}
