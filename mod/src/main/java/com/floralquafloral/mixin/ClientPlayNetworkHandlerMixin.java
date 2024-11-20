package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMarioClient;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.registries.stomp.StompHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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

	@WrapOperation(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
	private MutableText adjustDismountHint(String key, Object[] args, Operation<MutableText> original) {
		MarioMainClientData data = MarioMainClientData.getInstance();
		if(data == null || !data.isEnabled()) return original.call(key, args);

		GameOptions options = MinecraftClient.getInstance().options;
		return Text.translatable("mount.onboard.mario", options.sneakKey.getBoundKeyLocalizedText(), options.jumpKey.getBoundKeyLocalizedText());
	}
}
