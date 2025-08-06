package com.fqf.mario_qua_mario.mariodata.util;

import com.fqf.mario_qua_mario.mariodata.IMarioClientDataImpl;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.math.MathHelper;

public class WallSlidingSoundInstance extends AbstractSlidingSoundInstance {
	public WallSlidingSoundInstance(IMarioClientDataImpl data) {
		super(MarioSFX.SKID, data);
	}

	@Override
	protected boolean isFloorDependent() {
		return false;
	}

	@Override
	protected void updatePitchVolume() {
		this.pitch = 0.2F;
		this.volume = MathHelper.clamp((float) Math.abs(this.MARIO.mqm$getMarioData().getYVel()), 0.4F, 1.0F);
	}
}
