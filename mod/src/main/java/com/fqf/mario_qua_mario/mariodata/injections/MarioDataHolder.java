package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import org.jetbrains.annotations.NotNull;

public interface MarioDataHolder {
	@NotNull MarioPlayerData mqm$getMarioData();
	void mqm$setMarioData(MarioPlayerData replacementData);
}
