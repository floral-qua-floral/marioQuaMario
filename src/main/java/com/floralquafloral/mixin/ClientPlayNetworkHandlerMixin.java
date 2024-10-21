package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMarioClient;
import com.floralquafloral.registries.stomp.StompHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	// Code stolen from FlatteringAnvils by ItsFelix5
	@Redirect(method = "onEntityDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V"))
	private void onDamaged(Entity instance, DamageSource damageSource) {
		if(damageSource.isIn(StompHandler.FLATTENS_ENTITIES_TAG)) {
			MarioQuaMarioClient.SQUASHED_ENTITIES.add(instance);
		}
		instance.onDamaged(damageSource);
	}
}
