package com.fqf.charapoweract.mariodata.injections;

import com.fqf.charapoweract.mariodata.MarioMainClientData;
import org.jetbrains.annotations.NotNull;

public interface AdvMarioMainClientDataHolder extends AdvMarioAbstractClientDataHolder {
	default @NotNull MarioMainClientData mqm$getMarioData() {
		throw new AssertionError("AdvMarioMainClientDataHolder default method called?!");
	}
}
