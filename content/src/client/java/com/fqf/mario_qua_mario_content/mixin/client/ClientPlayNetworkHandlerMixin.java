package com.fqf.mario_qua_mario_content.mixin.client;

import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.item.ModItems;
import com.fqf.mario_qua_mario_content.util.MQMContentTags;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import com.fqf.mario_qua_mario_content.util.Squashable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow private ClientWorld world;

	@WrapOperation(method = "onEntityDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V"))
	private void squashFromSquashingDamage(Entity instance, DamageSource damageSource, Operation<Void> original) {
		if(instance instanceof LivingEntity livingInstance && damageSource.isIn(MQMContentTags.FLATTENS_ENTITIES))
			((Squashable) livingInstance).mqm$squash();
		original.call(instance, damageSource);
	}

	@Inject(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
	private void alternatePickupSoundForCoin(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
		Entity pickedUp = this.world.getEntityById(packet.getEntityId());
		if(pickedUp instanceof ItemEntity pickedUpItem && pickedUpItem.getStack().isOf(ModItems.COIN)) {
			this.world
					.playSound(
							pickedUp.getX(),
							pickedUp.getY(),
							pickedUp.getZ(),
							MarioContentSFX.COIN,
							SoundCategory.PLAYERS,
							0.7F,
							1,
							false
					);
		}
	}
}
