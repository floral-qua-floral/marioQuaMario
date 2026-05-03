package com.fqf.charapoweract_api.cpadata.injections;

import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;

public interface ICPAAuthoritativeDataHolder extends ICPADataHolder {
	default ICPAAuthoritativeData cpa$getICPAAuthoritativeData() {
		throw new IllegalStateException("The getICPAAuthoritativeData method is not implemented?! This may indicate you're missing the CPA Engine mod.");
	}
}
