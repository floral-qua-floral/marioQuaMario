package com.fqf.charapoweract.mariodata.injections;

import com.fqf.charapoweract.mariodata.MarioAnimationData;
import com.fqf.charapoweract.mariodata.MarioPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvMarioAbstractClientDataHolder extends AdvMarioDataHolder {
	default @NotNull MarioPlayerData mqm$getMarioData() {
		throw new AssertionError("AdvMarioAbstractClientDataHolder default method called?!");
	}

	default @NotNull MarioAnimationData mqm$getAnimationData() {
		throw new AssertionError("AdvMarioAbstractClientDataHolder default method (animation) called?!");
	}
}
