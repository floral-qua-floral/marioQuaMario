package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.util.CustomToadUtil;
import com.fqf.mario_qua_mario.util.Powers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public class TintFirstPersonHandMixin {
	@WrapOperation(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
	private void renderTintedArmForCustomToad(ModelPart instance, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, Operation<Void> original, @Local(argsOnly = true) AbstractClientPlayerEntity player) {
		if(player.cfa$getCfaData().hasPower(Powers.USES_TOAD_CUSTOMIZATIONS))
			instance.render(matrices, vertices, light, overlay, ((CustomToadUtil.CustomToadEntity) player).mqm$getToadData(CustomToadUtil.SKIN_COLOR));
		else
			original.call(instance, matrices, vertices, light, overlay);
	}
}
