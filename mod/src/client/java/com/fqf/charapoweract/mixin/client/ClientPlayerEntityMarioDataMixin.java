package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract.mariodata.MarioMainClientData;
import com.fqf.charapoweract.mariodata.MarioPlayerData;
import com.fqf.charapoweract.mariodata.injections.AdvMarioMainClientDataHolder;
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
public class ClientPlayerEntityMarioDataMixin implements AdvMarioMainClientDataHolder, ICPATravelDataHolder, ICPAClientDataHolder {
	@Unique private MarioMainClientData marioData = new MarioMainClientData((ClientPlayerEntity) (Object) this);

	@Override public ICPATravelData cpa$getICPATravelData() {
		return this.mqm$getMarioData();
	}
	@Override public ICPAClientData cpa$getICPAClientData() {
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
