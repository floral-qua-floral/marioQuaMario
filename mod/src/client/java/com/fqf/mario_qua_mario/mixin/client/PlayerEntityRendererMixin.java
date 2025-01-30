package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.mariodata.MarioAnimationData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
	@Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At("TAIL"))
	private void applyEverythingMutator(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
		abstractClientPlayerEntity.mqm$getAnimationData().rotateTotalPlayermodel(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true), abstractClientPlayerEntity, matrixStack);
//		MarioPlayerData data = abstractClientPlayerEntity.mqm$getMarioData();
//		if(!data.isEnabled() || data.getAction().ANIMATION == null) return;
//		MarioAnimationData animData = abstractClientPlayerEntity.mqm$getAnimationData();
//
//		matrixStack.translate(
//				animData.prevFrameAnimationDeltas.EVERYTHING.x,
//				animData.prevFrameAnimationDeltas.EVERYTHING.y,
//				animData.prevFrameAnimationDeltas.EVERYTHING.z
//		);
//		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(animData.prevFrameAnimationDeltas.EVERYTHING.yaw));
//		if(animData.prevFrameAnimationDeltas.EVERYTHING.pitch != 0 || animData.prevFrameAnimationDeltas.EVERYTHING.roll != 0) {
//			double halfHeight = abstractClientPlayerEntity.getBoundingBox(EntityPose.STANDING).getLengthY() / 2;
//			matrixStack.translate(0, halfHeight, 0);
//			matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(animData.prevFrameAnimationDeltas.EVERYTHING.pitch));
//			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(animData.prevFrameAnimationDeltas.EVERYTHING.roll));
//			matrixStack.translate(0, -halfHeight, 0);
//		}
	}
}
