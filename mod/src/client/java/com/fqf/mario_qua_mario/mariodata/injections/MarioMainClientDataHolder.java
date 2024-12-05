package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import org.jetbrains.annotations.NotNull;

public interface MarioMainClientDataHolder extends MarioDataHolder {
	@Override @NotNull MarioMainClientData mqm$getMarioData();
}
