package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.AdvMarioMainClientDataHolder;
import com.fqf.mario_qua_mario_api.mariodata.injections.IMarioClientDataHolder;
import com.fqf.mario_qua_mario_api.mariodata.injections.IMarioTravelDataHolder;
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
public class ClientPlayerEntityMarioDataMixin implements AdvMarioMainClientDataHolder, IMarioTravelDataHolder, IMarioClientDataHolder {
	@Unique private MarioMainClientData marioData = new MarioMainClientData((ClientPlayerEntity) (Object) this);

	@Override public IMarioTravelData mqm$getIMarioTravelData() {
		return this.mqm$getMarioData();
	}
	@Override public IMarioClientData mqm$getIMarioClientData() {
		return this.mqm$getMarioData();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting, CallbackInfo ci) {
		this.mqm$setMarioData(this.marioData);
	}

	@Override
	public @NotNull MarioMainClientData mqm$getMarioData() {
		return this.marioData;
	}

	@Override
	public void mqm$setMarioData(MarioPlayerData replacementData) {
		this.marioData = (MarioMainClientData) replacementData;
		replacementData.initialApply();
	}
}
