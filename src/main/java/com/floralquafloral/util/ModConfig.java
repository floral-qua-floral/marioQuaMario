package com.floralquafloral.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "qua_mario")
public class ModConfig implements ConfigData {
	private boolean addDebugInfo = true;

	private int bufferLength = 5;

	public boolean shouldAddDebugInfo() {
		return this.addDebugInfo;
	}
	public int getBufferLength() {
		return this.bufferLength;
	}
}
