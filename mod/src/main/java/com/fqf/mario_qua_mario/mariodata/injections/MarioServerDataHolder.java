package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import org.jetbrains.annotations.NotNull;

public interface MarioServerDataHolder extends MarioDataHolder {
	@SuppressWarnings("DataFlowIssue") @Override
	default @NotNull MarioServerPlayerData mqm$getMarioData() {
		return null;
	};
}
