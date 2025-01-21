package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import org.jetbrains.annotations.NotNull;

public interface AdvMarioMainClientDataHolder extends AdvMarioAbstractClientDataHolder {
	default @NotNull MarioMainClientData mqm$getMarioData() {
		throw new AssertionError("AdvMarioMainClientDataHolder default method called?!");
	}
}
