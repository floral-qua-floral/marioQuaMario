package com.fqf.charapoweract_api.mariodata.injections;

import com.fqf.charapoweract_api.mariodata.IMarioData;

public interface IMarioDataHolder {
	default IMarioData mqm$getIMarioData() {
		throw new IllegalStateException("This method will be implemented by the Mario qua Mario mod.");
	}
}
