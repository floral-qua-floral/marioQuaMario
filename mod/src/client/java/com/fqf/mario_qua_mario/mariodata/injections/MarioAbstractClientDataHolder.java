package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioAnimationData;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import org.jetbrains.annotations.NotNull;

public interface MarioAbstractClientDataHolder extends MarioDataHolder {
	default @NotNull MarioPlayerData mqm$getMarioData() {
		throw new AssertionError("MarioAbstractClientDataHolder default method called?!");
	}

	default @NotNull MarioAnimationData mqm$getAnimationData() {
		throw new AssertionError("MarioAbstractClientDataHolder default method (animation) called?!");
	}
}
