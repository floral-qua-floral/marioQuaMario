package com.fqf.charapoweract_api.cpadata.injections;

import com.fqf.charapoweract_api.cpadata.ICPATravelData;

public interface ICPATravelDataHolder extends ICPADataHolder {
	// TODO This interface is bad and should be removed at some point. Accessing travelData at an unexpected time will only cause problems.
	default ICPATravelData cpa$getICPATravelData() {
		throw new IllegalStateException("The getICPATravelData method is not implemented?! This may indicate you're missing the CPA Engine mod.");
	}
}
