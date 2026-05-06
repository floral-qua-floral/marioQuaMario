package com.fqf.charaformact.cfadata.injections;

import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvCfaServerDataHolder extends AdvCfaDataHolder {
	default @NotNull CfaServerPlayerData cfa$getCfaData() {
		throw new AssertionError("AdvCfaServerDataHolder default method called?!");
	};
}
