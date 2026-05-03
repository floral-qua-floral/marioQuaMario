package com.fqf.charapoweract_api.mariodata.injections;

import com.fqf.charapoweract_api.mariodata.IMarioAuthoritativeData;

public interface IMarioAuthoritativeDataHolder extends IMarioDataHolder {
	default IMarioAuthoritativeData mqm$getIMarioAuthoritativeData() {
		throw new IllegalStateException("This method will be implemented by the Mario qua Mario mod.");
	}
}
