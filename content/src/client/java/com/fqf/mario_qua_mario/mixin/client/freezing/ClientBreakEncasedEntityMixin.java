package com.fqf.mario_qua_mario.mixin.client.freezing;

import com.fqf.mario_qua_mario.freezing.BreakableEntity;
import com.fqf.mario_qua_mario.freezing.EncasementMiningHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientBreakEncasedEntityMixin {
	@Shadow @Nullable public HitResult crosshairTarget;

	@Shadow @Nullable public ClientPlayerEntity player;
	@Unique private final EncasementMiningHandler ENCASEMENT_MINING_HANDLER = new EncasementMiningHandler();

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"))
	private void handleEncasedEntityBreaking(boolean breaking, CallbackInfo ci) {
		this.ENCASEMENT_MINING_HANDLER.handleEncasedEntityBreaking(this.player, breaking, this.crosshairTarget);
		// We don't really need any extra logic right here - vanilla will already cancel block mining 'cause we're not
		// targeting a block.
	}

	@WrapWithCondition(
			method = "doAttack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"
			)
	)
	private boolean cancelAttackAgainstBreakableEntity(ClientPlayerInteractionManager instance, PlayerEntity player, Entity target) {
		return !((BreakableEntity) target).mqm$canMine();
	}
}
