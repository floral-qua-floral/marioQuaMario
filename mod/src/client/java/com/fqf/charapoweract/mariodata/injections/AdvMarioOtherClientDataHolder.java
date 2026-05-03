package com.fqf.charapoweract.mariodata.injections;

import com.fqf.charapoweract.mariodata.MarioOtherClientData;
import org.jetbrains.annotations.NotNull;

public interface AdvMarioOtherClientDataHolder extends AdvMarioAbstractClientDataHolder {
	default @NotNull MarioOtherClientData mqm$getMarioData() {
		throw new AssertionError("AdvMarioOtherClientDataHolder default method called?!");
	}
}
