package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.registries.states.AttackInterceptionHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow @Nullable public ClientPlayerEntity player;
	@Shadow @Nullable public HitResult crosshairTarget;

	@Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"), cancellable = true)
	public void interceptUnarmedAttack(CallbackInfoReturnable<Boolean> cir) {
		assert player != null && crosshairTarget != null;
		if(!player.getWeaponStack().isEmpty()) return;

		Entity hitEntity = null;
		BlockPos hitBlock = null;
		switch(crosshairTarget.getType()) {
			// This is weird but it's how vanilla does it so i guess i will too????
			case ENTITY -> hitEntity = ((EntityHitResult) crosshairTarget).getEntity();
			case BLOCK -> hitBlock = ((BlockHitResult) crosshairTarget).getBlockPos();
		}

		MarioMainClientData data = (MarioMainClientData) MarioDataManager.getMarioData(player);
		float attackCooldownProgress = player.getAttackCooldownProgress(0.5F);
		if(
				(AttackInterceptionHandler.attemptInterceptions(
						data, attackCooldownProgress, data.getAction().INTERCEPTIONS, true, hitEntity, hitBlock)
				|| AttackInterceptionHandler.attemptInterceptions(
						data, attackCooldownProgress, data.getPowerUp().INTERCEPTIONS, false, hitEntity, hitBlock))
				&& hitBlock == null
		) {
			cir.setReturnValue(false);
		}
	}
}
