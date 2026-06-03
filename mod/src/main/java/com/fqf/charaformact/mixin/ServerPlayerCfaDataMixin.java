package com.fqf.charaformact.mixin;

import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact.cfadata.injections.AdvCfaServerDataHolder;
import com.fqf.charaformact_api.cfadata.injections.CfaAuthoritativeDataHolder;
import com.fqf.charaformact_api.cfadata.injections.CfaTravelDataHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerCfaDataMixin implements AdvCfaServerDataHolder, CfaAuthoritativeDataHolder, CfaTravelDataHolder {
	@Unique private CfaServerPlayerData cfaServerData = new CfaServerPlayerData((ServerPlayerEntity) (Object) this);

	@Override public CfaAuthoritativeData cfa$getCfaAuthoritativeData() {
		return this.cfa$getCfaData();
	}

	@Override public @NotNull CfaServerPlayerData cfa$getCfaData() {
		return this.cfaServerData;
	}

}
