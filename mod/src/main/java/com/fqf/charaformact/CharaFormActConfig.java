package com.fqf.charaformact;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "charaformact")
public class CharaFormActConfig implements ConfigData {
	private int bufferLength = 3;
	private boolean logAllActionTransitions = false;
	private boolean logAppearancesUVs = false;
	private boolean logNBTReadWrite = false;
	private boolean logActionTransitionInjections = false;
	private boolean specialHUD = false;
	private boolean allowIllegalTransitionsInSingleplayer = true;
	private float inherentBumpedBlockScale = 1.0001F;
	private int bumpedBlockLingerFrames = 2;
	private boolean logFeatureContexts = true;
	private boolean suppressVoiceUnderwater = true;

	public boolean logAllActionTransitions() {
		return this.logAllActionTransitions;
	}
	public boolean logCharacterFormModelUVs() {
		return this.logAppearancesUVs;
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
	public boolean logFeatureContexts() {
		return this.logFeatureContexts;
	}
	public boolean doSuppressVoiceUnderwater() {
		return this.suppressVoiceUnderwater;
	}
	public int getBumpedBlockLingerFrames() {
		return this.bumpedBlockLingerFrames;
	}
}
