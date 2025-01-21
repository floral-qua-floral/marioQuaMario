package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.IMarioClientData;

public interface IMarioClientDataHolder extends IMarioDataHolder {
	default IMarioClientData mqm$getIMarioClientData() {
		throw new IllegalStateException("This method will be implemented by the Mario qua Mario mod.");
	}
}
