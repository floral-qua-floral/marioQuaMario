package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.cfadata.CfaMainClientData;
import com.fqf.charaformact.registries.actions.parsed.ParsedMountedAction;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Shadow public abstract ClientWorld getWorld();

	@Shadow private ClientWorld world;

	@WrapOperation(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
	private MutableText adjustDismountHint(String key, Object[] args, Operation<MutableText> original) {
		ClientPlayerEntity mainPlayer = MinecraftClient.getInstance().player;
		if(mainPlayer != null) {
			CfaMainClientData data = mainPlayer.cfa$getCfaData();
			if(data.isEnabled() && data.getActionCategory() == ActionCategory.MOUNTED) {
				return ((ParsedMountedAction) data.getAction()).DISMOUNT_HINT;
			}
		}
		return original.call(key, args);
	}
}
