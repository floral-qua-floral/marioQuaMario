package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import org.jetbrains.annotations.NotNull;

public interface MarioDataHolder {
	@SuppressWarnings("DataFlowIssue")
	default @NotNull MarioPlayerData mqm$getMarioData() {
		return null;
	};
	default void mqm$setMarioData(MarioPlayerData replacementData) {

	};
}
