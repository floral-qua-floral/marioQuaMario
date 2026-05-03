package com.fqf.charapoweract_api.cpadata.injections;

import com.fqf.charapoweract_api.cpadata.ICPAClientData;

public interface ICPAClientDataHolder extends ICPADataHolder {
	default ICPAClientData cpa$getICPAClientData() {
		throw new IllegalStateException("The getICPAClientData method is not implemented?! This may indicate you're missing the CPA Engine mod.");
	}
}
