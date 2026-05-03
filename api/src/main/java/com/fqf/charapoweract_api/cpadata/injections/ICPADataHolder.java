package com.fqf.charapoweract_api.cpadata.injections;

import com.fqf.charapoweract_api.cpadata.ICPAData;

public interface ICPADataHolder {
	default ICPAData cpa$getICPAData() {
		throw new IllegalStateException("The getICPAData method is not implemented?! This may indicate you're missing the CPA Engine mod.");
	}
}
