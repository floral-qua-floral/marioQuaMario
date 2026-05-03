package com.fqf.charapoweract.mariodata.injections;

import com.fqf.charapoweract.mariodata.MarioServerPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvMarioServerDataHolder extends AdvMarioDataHolder {
	default @NotNull MarioServerPlayerData mqm$getMarioData() {
		throw new AssertionError("AdvMarioServerDataHolder default method called?!");
	};
}
