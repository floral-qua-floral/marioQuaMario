package com.fqf.charapoweract_api.mariodata.injections;

import com.fqf.charapoweract_api.mariodata.IMarioTravelData;

public interface IMarioTravelDataHolder extends IMarioDataHolder {
	// TODO This interface is bad and should be removed when the API is exposed. Maybe ReadableMotion equivalent too.
	default IMarioTravelData mqm$getIMarioTravelData() {
		throw new IllegalStateException("This method will be implemented by the Mario qua Mario mod.");
	}
}
