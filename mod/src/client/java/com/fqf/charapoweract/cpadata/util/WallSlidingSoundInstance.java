package com.fqf.charapoweract.cpadata.util;

import com.fqf.charapoweract.cpadata.ICPAClientDataImpl;
import com.fqf.charapoweract.util.CPASounds;
import net.minecraft.util.math.MathHelper;

public class WallSlidingSoundInstance extends AbstractSlidingSoundInstance {
	public WallSlidingSoundInstance(ICPAClientDataImpl data) {
		super(CPASounds.SKID, data);
	}

	@Override
	protected boolean isFloorDependent() {
		return false;
	}

	@Override
	protected void updatePitchVolume() {
		this.pitch = 0.2F;
		this.volume = MathHelper.clamp((float) Math.abs(this.PLAYER.cpa$getCPAData().getYVel()), 0.4F, 1.0F);
	}
}
