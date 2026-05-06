package com.fqf.charaformact.cfadata.injections;

import com.fqf.charaformact.cfadata.CfaMainClientData;
import org.jetbrains.annotations.NotNull;

public interface AdvCfaMainClientDataHolder extends AdvCfaAbstractClientDataHolder {
	default @NotNull CfaMainClientData cfa$getCfaData() {
		throw new AssertionError("AdvCfaMainClientDataHolder default method called?!");
	}
}
