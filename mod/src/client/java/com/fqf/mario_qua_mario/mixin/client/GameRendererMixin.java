package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final MinecraftClient client;

	@WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
	private void scaleBobbing(MatrixStack instance, float x, float y, float z, Operation<Void> original) {
		MarioPlayerData data = ((PlayerEntity) Objects.requireNonNull(this.client.getCameraEntity())).mqm$getMarioData();
		float horizontalScale = data.getHorizontalScale();
		original.call(instance, x * horizontalScale, y * data.getVerticalScale(), z * horizontalScale);
	}
}
