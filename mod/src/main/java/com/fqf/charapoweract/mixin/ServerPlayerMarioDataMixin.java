package com.fqf.charapoweract.mixin;

import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract.mariodata.MarioPlayerData;
import com.fqf.charapoweract.mariodata.MarioServerPlayerData;
import com.fqf.charapoweract.mariodata.injections.AdvMarioServerDataHolder;
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
public class ServerPlayerMarioDataMixin implements AdvMarioServerDataHolder, ICPAAuthoritativeDataHolder, ICPATravelDataHolder {
	@Unique private MarioServerPlayerData marioServerData = new MarioServerPlayerData((ServerPlayerEntity) (Object) this);

	@Override public ICPAAuthoritativeData cpa$getICPAAuthoritativeData() {
		return this.mqm$getMarioData();
	}
	@Override public ICPATravelData cpa$getICPATravelData() {
		return this.mqm$getMarioData();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
//		this.mqm$setMarioData(this.marioServerData);
	}

	@Override public @NotNull MarioServerPlayerData mqm$getMarioData() {
		return this.marioServerData;
	}

	@Override public void mqm$setMarioData(MarioPlayerData replacementData) {
		this.marioServerData = (MarioServerPlayerData) replacementData;
		ServerPlayerEntity mario = (ServerPlayerEntity) (Object) this;
		replacementData.initialApply();
	}
}
