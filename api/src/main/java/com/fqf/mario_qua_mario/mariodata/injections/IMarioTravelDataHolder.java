package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;

public interface IMarioTravelDataHolder extends IMarioDataHolder {
	default IMarioTravelData mqm$getIMarioTravelData() {
		throw new IllegalStateException("This method will be implemented by the Mario qua Mario mod.");
	}
}
