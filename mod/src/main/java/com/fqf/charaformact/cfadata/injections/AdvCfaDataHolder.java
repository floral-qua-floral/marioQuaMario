package com.fqf.charaformact.cfadata.injections;

import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.injections.CfaDataHolder;

public interface AdvCfaDataHolder extends CfaDataHolder {
	CfaPlayerData cfa$getCfaData();

	default void cfa$setCfaData(CfaPlayerData replacementData) {
		throw new AssertionError("AdvCfaDataHolder default method called?!");
	};
}
