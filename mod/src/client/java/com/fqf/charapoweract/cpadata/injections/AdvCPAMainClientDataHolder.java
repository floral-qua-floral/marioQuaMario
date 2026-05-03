package com.fqf.charapoweract.cpadata.injections;

import com.fqf.charapoweract.cpadata.CPAMainClientData;
import org.jetbrains.annotations.NotNull;

public interface AdvCPAMainClientDataHolder extends AdvCPAAbstractClientDataHolder {
	default @NotNull CPAMainClientData cpa$getCPAData() {
		throw new AssertionError("AdvCPAMainClientDataHolder default method called?!");
	}
}
