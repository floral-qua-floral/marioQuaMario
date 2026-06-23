package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.AppearanceRenderer;
import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRendererMixin<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
	@WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/model/ModelPart;Z)Lnet/minecraft/client/render/entity/model/PlayerEntityModel;"))
	private static PlayerEntityModel<AbstractClientPlayerEntity> associatedWithAppearanceModel(ModelPart root, boolean thinArms, Operation<PlayerEntityModel<AbstractClientPlayerEntity>> original, @Local(argsOnly = true) EntityRendererFactory.Context ctx) {
		ParsedClientAppearance currentCustomModel = ClientAppearanceCollector.INSTANCE.getCurrentlyInitializingAppearance();
		if (currentCustomModel == null) {
			CharaFormAct.LOGGER.info("Instantiating a vanilla player renderer, with {} arms!", thinArms ? "thin" : "wide");
			return original.call(root, thinArms);
		} else {
			CharaFormAct.LOGGER.info("Instantiating an Appearance-based player renderer!");
			return currentCustomModel.makeAndGetModel(ctx);
		}
	}

	@Unique private boolean capturingFeatures;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void prepareToCatchAddedFeatures(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
		if(ClientAppearanceCollector.INSTANCE.getCurrentlyInitializingAppearance() == null && !slim) {
			CharaFormAct.LOGGER.info("Vanilla's wide-armed player renderer will now start capturing any additional features...");
			this.capturingFeatures = true;
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

	@WrapOperation(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/PlayerEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V"))
	private <T extends LivingEntity> void preventCfaAnimations(
			PlayerEntityModel<?> instance, T entity,
			float limbAngle, float limbDistance, float animationProgress,
			float headYaw, float headPitch,
			Operation<Void> original
	) {
		CfaAppearanceData<?> data;
		if(entity instanceof AbstractClientPlayerEntity player) data = player.cfa$getAppearanceData();
		else data = null;

		if(data != null) data.doingFirstPersonHand = true;
		original.call(instance, entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		if(data != null) data.doingFirstPersonHand = false;
	}

	@WrapOperation(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;texture()Lnet/minecraft/util/Identifier;"))
	private Identifier getActualTexturePls(SkinTextures instance, Operation<Identifier> original, @Local(argsOnly = true) AbstractClientPlayerEntity player) {
		if((Object) this instanceof AppearanceRenderer appearanceRenderer)
			return appearanceRenderer.TEXTURE_FUNCTION.apply(player);
		return original.call(instance);
	}

	@Override
	protected boolean isCapturingFeatures() {
		return this.capturingFeatures;
	}
}
