package com.fqf.mario_qua_mario.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "mario_qua_mario")
public class MQMConfig implements ConfigData {
	private boolean logAllActionTransitions = false;
	private boolean specialHUD = false;

	public boolean logAllActionTransitions() {
		return this.logAllActionTransitions;
	}
	public boolean isSpecialHUDEnabled() {
		return this.specialHUD;
	}
}
