package com.fqf.charaformact;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "charaformact")
public class CharaFormActConfig implements ConfigData {
	private int bufferLength = 3;
	private boolean logAllActionTransitions = false;
	private boolean logNBTReadWrite = false;
	private boolean logActionTransitionInjections = false;
	private boolean specialHUD = false;
	private boolean allowIllegalTransitionsInSingleplayer = true;
	private float inherentBumpedBlockScale = 1.0001F;

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
	public boolean shouldAllowIllegalTransitionsInSingleplayer() {
		return this.allowIllegalTransitionsInSingleplayer;
	}
	public float getInherentBumpedBlockScale() {
		return this.inherentBumpedBlockScale;
	}
}
