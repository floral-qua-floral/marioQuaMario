package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import com.fqf.charaformact.util.CfaSounds;
import net.minecraft.util.math.MathHelper;

public class WallSlidingSoundInstance extends AbstractSlidingSoundInstance {
	public WallSlidingSoundInstance(CfaClientDataImpl data) {
		super(CfaSounds.SKID, data);
	}

	@Override
	protected boolean isFloorDependent() {
		return false;
	}

	@Override
	protected void updatePitchVolume() {
		this.pitch = 0.2F;
		this.volume = MathHelper.clamp((float) Math.abs(this.PLAYER.cfa$getCfaData().getYVel()), 0.4F, 1.0F);
	}
}
