package com.fqf.charapoweract.mixin;

import com.fqf.charapoweract.cpadata.CPAServerPlayerData;
import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract.cpadata.injections.AdvCPAServerDataHolder;
import com.fqf.charapoweract_api.cpadata.injections.ICPAAuthoritativeDataHolder;
import com.fqf.charapoweract_api.cpadata.injections.ICPATravelDataHolder;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerCPADataMixin implements AdvCPAServerDataHolder, ICPAAuthoritativeDataHolder, ICPATravelDataHolder {
	@Unique private CPAServerPlayerData cpaServerData = new CPAServerPlayerData((ServerPlayerEntity) (Object) this);

	@Override public ICPAAuthoritativeData cpa$getICPAAuthoritativeData() {
		return this.cpa$getCPAData();
	}
	@Override public ICPATravelData cpa$getICPATravelData() {
		return this.cpa$getCPAData();
	}

	@Override public @NotNull CPAServerPlayerData cpa$getCPAData() {
		return this.cpaServerData;
	}

	@Override public void cpa$setCPAData(CPAPlayerData replacementData) {
		this.cpaServerData = (CPAServerPlayerData) replacementData;
		replacementData.initialApply();
	}
}
