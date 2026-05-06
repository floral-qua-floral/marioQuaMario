package com.fqf.charaformact_api.cfadata.injections;

import com.fqf.charaformact_api.cfadata.CfaClientData;

public interface CfaClientDataHolder extends CfaDataHolder {
	default CfaClientData cfa$getCfaClientData() {
		throw new IllegalStateException("The getCfaClientData method is not implemented?! This may indicate you're missing the CFA Engine mod.");
	}
}
