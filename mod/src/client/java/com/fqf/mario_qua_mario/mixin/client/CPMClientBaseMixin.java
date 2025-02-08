package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.tom.cpm.client.ClientBase;
import com.tom.cpm.shared.definition.ModelDefinition;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientBase.class)
public class CPMClientBaseMixin {
	@Inject(method = "renderCape", at = @At(value = "INVOKE", target = "Lcom/tom/cpm/client/PlayerRenderManager;setModelPose(Ljava/lang/Object;)V"))
	private static void animateCapeAsTail(
			MatrixStack matrixStack,
			VertexConsumer buffer,
			int packedLightIn,
			AbstractClientPlayerEntity abstractClientPlayerEntity,
			float partialTicks,
			PlayerEntityModel<AbstractClientPlayerEntity> model,
			ModelDefinition modelDefinition,
			CallbackInfo ci
	) {
		abstractClientPlayerEntity.mqm$getAnimationData().animateTail(partialTicks, abstractClientPlayerEntity, model.cloak, model.body, model.rightLeg, model.leftLeg);
	}
}
