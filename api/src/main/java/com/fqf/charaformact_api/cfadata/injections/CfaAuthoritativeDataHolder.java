package com.fqf.charaformact_api.cfadata.injections;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;

public interface CfaAuthoritativeDataHolder extends CfaDataHolder {
	default CfaAuthoritativeData cfa$getCfaAuthoritativeData() {
		throw new IllegalStateException("The getCfaAuthoritativeData method is not implemented?! This may indicate you're missing the CFA Engine mod.");
	}
}
