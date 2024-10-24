package com.floralquafloral.stats;

import com.floralquafloral.mariodata.MarioData;

public interface CharaStat {
	double getDefaultValue();
	default double getValue(MarioData data) {
		return this.getDefaultValue() * this.getMultiplier(data);
	};
	default double getAsThreshold(MarioData data) {
		return this.getValue(data) * 0.99;
	}
	default double getAsLimit(MarioData data) {
		return this.getValue(data) * 1.015;
	}
	double getMultiplier(MarioData data);
}
