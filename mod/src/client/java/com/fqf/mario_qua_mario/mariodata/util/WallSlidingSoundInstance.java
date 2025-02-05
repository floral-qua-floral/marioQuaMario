package com.fqf.mario_qua_mario.mariodata.util;

import com.fqf.mario_qua_mario.mariodata.IMarioClientDataImpl;
import com.fqf.mario_qua_mario.util.MarioSFX;

public class WallSlidingSoundInstance extends AbstractSlidingSoundInstance {
	public WallSlidingSoundInstance(IMarioClientDataImpl data) {
		super(MarioSFX.SKID_WALL, data);
	}

	@Override
	protected boolean isFloorDependent() {
		return false;
	}

	@Override
	protected void updatePitchVolume() {
		
	}
}
