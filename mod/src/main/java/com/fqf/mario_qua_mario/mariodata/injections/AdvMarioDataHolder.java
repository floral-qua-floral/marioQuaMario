package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvMarioDataHolder {
	default @NotNull MarioPlayerData mqm$getMarioData() {
		throw new AssertionError("AdvMarioDataHolder default method called?!");
	};
	default void mqm$setMarioData(MarioPlayerData replacementData) {
		throw new AssertionError("AdvMarioDataHolder default method called?!");
	};
}
