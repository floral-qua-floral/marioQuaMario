package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.forms.Raccoon;
import com.fqf.mario_qua_mario.util.Powers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Unique private static boolean canTargetProjectiles(PlayerEntity player) {
		return player != null && player.getWeaponStack().isEmpty() && player.cfa$getCfaData().hasPower(Powers.CAN_HIT_PROJECTILES);
	}

	@WrapOperation(method = "method_18144", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canHit()Z"))
	private static boolean allowHittingUnhittableProjectiles(Entity instance, Operation<Boolean> original) {
		boolean canHit = original.call(instance);
		if(canHit) return true;

		return canTargetProjectiles(MinecraftClient.getInstance().player) && Raccoon.canBeReflected(instance);
	}
}
