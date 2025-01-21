package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;

public interface IMarioAuthoritativeDataHolder extends IMarioDataHolder {
	default IMarioAuthoritativeData mqm$getIMarioAuthoritativeData() {
		throw new IllegalStateException("This method will be implemented by the Mario qua Mario mod.");
	}
}
