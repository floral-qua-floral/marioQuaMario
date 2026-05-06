package com.fqf.charaformact_api.cfadata.injections;

import com.fqf.charaformact_api.cfadata.CfaData;

public interface CfaDataHolder {
	default CfaData cfa$getCfaData() {
		throw new IllegalStateException("The getCfaData method is not implemented?! This may indicate you're missing the CFA Engine mod.");
	}
}
