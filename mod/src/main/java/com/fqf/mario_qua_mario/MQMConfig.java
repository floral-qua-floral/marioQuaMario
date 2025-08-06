package com.fqf.mario_qua_mario;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "mario_qua_mario")
public class MQMConfig implements ConfigData {
	private int bufferLength = 3;
	private boolean logAllActionTransitions = false;
	private boolean logNBTReadWrite = false;
	private boolean logActionTransitionInjections = false;
	private boolean specialHUD = false;

	public boolean logAllActionTransitions() {
		return this.logAllActionTransitions;
	}
	public boolean logNBTReadWrite() {
		return this.logNBTReadWrite;
	}
	public boolean logActionTransitionInjections() {
		return this.logActionTransitionInjections;
	}
	public boolean isSpecialHUDEnabled() {
		return this.specialHUD;
	}
	public int getBufferLength() {
		return this.bufferLength;
	}
}
