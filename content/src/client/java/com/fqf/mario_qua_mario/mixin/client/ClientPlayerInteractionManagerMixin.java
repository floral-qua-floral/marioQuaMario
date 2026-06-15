package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
	private void doNotNetworkAttacksOnInvalidTargets(PlayerEntity player, Entity target, CallbackInfo ci) {
		if(!target.canHit()) {
			MarioQuaMario.LOGGER.error("""
							Client player tried to network an attack on unhittable entity! This should never be allowed to happen!
							\tTarget: {}
							\tForm: {}
							\tWeapon: {}""",
					target, player.cfa$getCfaData().getFormID(), player.getWeaponStack()
			);
			if(!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				// In a non-development environment, prevent the kick. But we want it to happen in dev so we notice it.
				ci.cancel();
			}
		}
	}
}
