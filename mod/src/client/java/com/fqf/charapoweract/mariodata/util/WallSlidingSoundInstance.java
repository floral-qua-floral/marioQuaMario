package com.fqf.charapoweract.mariodata.util;

import com.fqf.charapoweract.mariodata.ICPAClientDataImpl;
import com.fqf.charapoweract.util.MarioSFX;
import net.minecraft.util.math.MathHelper;

public class WallSlidingSoundInstance extends AbstractSlidingSoundInstance {
	public WallSlidingSoundInstance(ICPAClientDataImpl data) {
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
