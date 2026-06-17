package com.fqf.mario_qua_mario.util;

public interface LightningStrikableEntity {
	default boolean mqm$resistLightningStrike() {
		return false;
	}
}
