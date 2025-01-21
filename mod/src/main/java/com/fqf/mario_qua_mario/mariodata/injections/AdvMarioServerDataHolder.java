package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvMarioServerDataHolder extends AdvMarioDataHolder {
	default @NotNull MarioServerPlayerData mqm$getMarioData() {
		throw new AssertionError("AdvMarioServerDataHolder default method called?!");
	};
}
