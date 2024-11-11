package com.floralquafloral.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "qua_mario")
public class ModConfig implements ConfigData {
	private boolean backflipFromVehicles = true;
	private boolean usePowerUpHearts = true;
	private int bufferLength = 5;

	public boolean canBackflipFromVehicles() {
		return this.backflipFromVehicles;
	}
	public boolean shouldUsePowerUpHearts() {
		return this.usePowerUpHearts;
	}
	public int getBufferLength() {
		return this.bufferLength;
	}
}
