package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract.cpadata.injections.AdvCPAMainClientDataHolder;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract.cpadata.CPAMainClientData;
import com.fqf.charapoweract_api.cpadata.injections.ICPAClientDataHolder;
import com.fqf.charapoweract_api.cpadata.injections.ICPATravelDataHolder;
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
public class ClientPlayerEntityCPADataMixin implements AdvCPAMainClientDataHolder, ICPATravelDataHolder, ICPAClientDataHolder {
	@Unique private CPAMainClientData marioData = new CPAMainClientData((ClientPlayerEntity) (Object) this);

	@Override public ICPATravelData cpa$getICPATravelData() {
		return this.cpa$getCPAData();
	}
	@Override public ICPAClientData cpa$getICPAClientData() {
		return this.cpa$getCPAData();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting, CallbackInfo ci) {
		this.cpa$setCPAData(this.marioData);
	}

	@Override
	public @NotNull CPAMainClientData cpa$getCPAData() {
		return this.marioData;
	}

	@Override
	public void cpa$setCPAData(CPAPlayerData replacementData) {
		this.marioData = (CPAMainClientData) replacementData;
		replacementData.initialApply();
	}
}
