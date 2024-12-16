package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import org.jetbrains.annotations.NotNull;

public interface MarioMainClientDataHolder extends MarioDataHolder {
	@SuppressWarnings("DataFlowIssue") @Override
	default @NotNull MarioMainClientData mqm$getMarioData() {
		return null;
	}
}
