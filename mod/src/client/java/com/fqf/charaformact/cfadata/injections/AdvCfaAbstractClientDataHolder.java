package com.fqf.charaformact.cfadata.injections;

import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import org.jetbrains.annotations.NotNull;

public interface AdvCfaAbstractClientDataHolder extends AdvCfaDataHolder {
	default @NotNull CfaPlayerData cfa$getCfaData() {
		throw new AssertionError("AdvCfaAbstractClientDataHolder default method called?!");
	}

	@SuppressWarnings("unchecked")
	default <T extends CfaPlayerData & CfaAnimatingData & CfaClientDataImpl> @NotNull T cfa$getCfaData2() {
		return (T) this.cfa$getCfaData();
	}

	default @NotNull CfaAppearanceData<?> cfa$getAppearanceData() {
		throw new AssertionError("AdvCfaAbstractClientDataHolder default method (appearance) called?!");
	}
}
