package com.fqf.mario_qua_mario.entity.custom;

import net.minecraft.entity.damage.DamageSource;

public interface FatalityReceiver {
	boolean makeFatal(DamageSource source, boolean isFromCommand);
}
