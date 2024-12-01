package com.floralquafloral;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public interface StompableEntity {
	StompResult qua_mario$stomp(PlayerEntity mario, Identifier stompType, DamageSource damageSource, float amount);

	/**
	 * NORMAL - The Stomp behaves normally. Would be returned by a Goomba.
	 * PAINFUL - Trying to Stomp this entity hurts Mario. Would be returned by a Spiny or a Piranha Plant.
	 * GLANCING - Used internally. Returning this is not recommended.
	 * RESISTED - Mario bounces off this entity without hurting it. Would be returned by a Wiggler after the first time it's been stomped.
	 * FAIL_IF_BASIC - Mario passes through the entity unless he's Ground Pounding. Would be returned by a Blockhopper (look it up).
	 * FAIL - Mario can't stomp this entity. Would be returned by a Boo.
	 * MOUNT - Trying to Stomp this entity instead Mounts it. Would be returned by Yoshi.
	 */
	enum StompResult {
		NORMAL,
		PAINFUL,
		GLANCING,
		RESISTED,
		FAIL,
		MOUNT
	}
}
