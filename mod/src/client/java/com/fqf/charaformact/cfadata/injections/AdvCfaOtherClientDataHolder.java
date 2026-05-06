package com.fqf.charaformact.cfadata.injections;

import com.fqf.charaformact.cfadata.CfaOtherClientData;
import org.jetbrains.annotations.NotNull;

public interface AdvCfaOtherClientDataHolder extends AdvCfaAbstractClientDataHolder {
	default @NotNull CfaOtherClientData cfa$getCfaData() {
		throw new AssertionError("AdvCfaOtherClientDataHolder default method called?!");
	}
}
