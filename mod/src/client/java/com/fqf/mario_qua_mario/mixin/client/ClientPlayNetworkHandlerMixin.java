package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.registries.actions.parsed.ParsedMountedAction;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@WrapOperation(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
	private MutableText adjustDismountHint(String key, Object[] args, Operation<MutableText> original) {
		ClientPlayerEntity mario = MinecraftClient.getInstance().player;
		if(mario != null) {
			MarioMainClientData data = mario.mqm$getMarioData();
			if(data.isEnabled() && data.getActionCategory() == ActionCategory.MOUNTED) {
				MutableText actionDismountHint = ((ParsedMountedAction) data.getAction()).getDismountHint();
				if(actionDismountHint != null) return actionDismountHint;
			}
		}
		return original.call(key, args);
	}
}
