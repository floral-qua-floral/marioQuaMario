package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.cfadata.CfaMainClientData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.cfadata.injections.AdvCfaMainClientDataHolder;
import com.fqf.charaformact_api.cfadata.injections.CfaClientDataHolder;
import com.fqf.charaformact_api.cfadata.injections.CfaTravelDataHolder;
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
public class ClientPlayerEntityCfaDataMixin implements AdvCfaMainClientDataHolder, CfaTravelDataHolder, CfaClientDataHolder {
	@Unique private CfaMainClientData cfaData = new CfaMainClientData((ClientPlayerEntity) (Object) this);

	@Override public CfaClientData cfa$getCfaClientData() {
		return this.cfa$getCfaData();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting, CallbackInfo ci) {
		this.cfa$setCfaData(this.cfaData);
	}

	@Override
	public @NotNull CfaMainClientData cfa$getCfaData() {
		return this.cfaData;
	}

	@Override
	public void cfa$setCfaData(CfaPlayerData replacementData) {
		this.cfaData = (CfaMainClientData) replacementData;
		replacementData.initialApply();
	}
}
