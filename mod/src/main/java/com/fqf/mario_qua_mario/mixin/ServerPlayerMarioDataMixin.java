package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.MarioServerDataHolder;
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
public class ServerPlayerMarioDataMixin implements MarioServerDataHolder {
	@Unique private MarioServerPlayerData marioServerData;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
		this.mqm$setMarioData(new MarioServerPlayerData((ServerPlayerEntity) (Object) this));
	}

	@Override public @NotNull MarioServerPlayerData mqm$getMarioData() {
		return this.marioServerData;
	}

	@Override public void mqm$setMarioData(MarioPlayerData replacementData) {
		this.marioServerData = (MarioServerPlayerData) replacementData;
		replacementData.setMario((ServerPlayerEntity) (Object) this);
	}
}
