package com.fqf.charaformact.cfadata.injections;

import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import org.jetbrains.annotations.NotNull;

public interface AdvCfaAbstractClientDataHolder extends AdvCfaDataHolder {
	default @NotNull CfaPlayerData cfa$getCfaData() {
		throw new AssertionError("AdvCfaAbstractClientDataHolder default method called?!");
	}

	@SuppressWarnings("unchecked")
	default <DataType extends CfaReadableMotionData & CfaClientDataImpl> @NotNull DataType cfa$getCfaData2() {
		return (DataType) this.cfa$getCfaData();
	}

	default @NotNull CfaAppearanceData<?> cfa$getAppearanceData() {
		throw new AssertionError("AdvCfaAbstractClientDataHolder default method (appearance) called?!");
	}
}
