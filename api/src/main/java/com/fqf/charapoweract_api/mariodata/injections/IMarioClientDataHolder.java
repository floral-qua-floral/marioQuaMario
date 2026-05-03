package com.fqf.charapoweract_api.mariodata.injections;

import com.fqf.charapoweract_api.mariodata.IMarioClientData;

public interface IMarioClientDataHolder extends IMarioDataHolder {
	default IMarioClientData mqm$getIMarioClientData() {
		throw new IllegalStateException("This method will be implemented by the Mario qua Mario mod.");
	}
}
