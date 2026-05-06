package com.fqf.charaformact.cfadata.injections;

import com.fqf.charaformact.cfadata.CfaAnimationData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvCfaAbstractClientDataHolder extends AdvCfaDataHolder {
	default @NotNull CfaPlayerData cfa$getCfaData() {
		throw new AssertionError("AdvCfaAbstractClientDataHolder default method called?!");
	}

	default @NotNull CfaAnimationData cfa$getAnimationData() {
		throw new AssertionError("AdvCfaAbstractClientDataHolder default method (animation) called?!");
	}
}
