package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import org.jetbrains.annotations.NotNull;

public interface MarioDataHolder {
	default @NotNull MarioPlayerData mqm$getMarioData() {
		throw new AssertionError("MarioDataHolder default method called?!");
	};
	default void mqm$setMarioData(MarioPlayerData replacementData) {
		throw new AssertionError("MarioDataHolder default method called?!");
	};
}
