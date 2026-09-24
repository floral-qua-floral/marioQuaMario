package com.fqf.mario_qua_mario.mixin.client.coin;

import com.fqf.mario_qua_mario.item.MQMItems;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class CoinPickupSoundMixin {
	@Shadow private ClientWorld world;

	@Inject(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
	private void alternatePickupSoundForCoin(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
		Entity pickedUp = this.world.getEntityById(packet.getEntityId());
		if(pickedUp instanceof ItemEntity pickedUpItem && pickedUpItem.getStack().isOf(MQMItems.COIN)) {
			this.world
					.playSound(
							pickedUp.getX(),
							pickedUp.getY(),
							pickedUp.getZ(),
							MarioSFX.COIN,
							SoundCategory.PLAYERS,
							0.7F,
							1,
							false
					);
		}
	}
}
