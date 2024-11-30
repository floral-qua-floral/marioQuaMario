package com.floralquafloral;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;

public interface StompableEntity {
	StompResult qua_mario$stomp(Identifier stompType, DamageSource damageSource, float amount);

	enum StompResult {
		NORMAL,
		PAINFUL,
		GLANCING,
		RESISTED,
		FAIL_IF_BASIC,
		FAIL
	}
}
