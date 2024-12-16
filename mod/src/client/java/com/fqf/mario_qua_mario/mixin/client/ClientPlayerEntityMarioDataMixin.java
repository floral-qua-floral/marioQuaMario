package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.mariodata.injections.MarioMainClientDataHolder;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mixin.PlayerEntityMarioDataMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMarioDataMixin implements MarioMainClientDataHolder {
	@Unique private MarioMainClientData marioData;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting, CallbackInfo ci) {
		mqm$setMarioData(new MarioMainClientData((ClientPlayerEntity) (Object) this));
	}

	@Override
	public @NotNull MarioMainClientData mqm$getMarioData() {
		return this.marioData;
	}

	@Override
	public void mqm$setMarioData(MarioPlayerData replacementData) {
		this.marioData = (MarioMainClientData) replacementData;
		replacementData.setMario((ClientPlayerEntity) (Object) this);
	}
}
