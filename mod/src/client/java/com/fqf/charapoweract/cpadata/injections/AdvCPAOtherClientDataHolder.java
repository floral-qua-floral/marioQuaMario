package com.fqf.charapoweract.cpadata.injections;

import com.fqf.charapoweract.cpadata.CPAOtherClientData;
import org.jetbrains.annotations.NotNull;

public interface AdvCPAOtherClientDataHolder extends AdvCPAAbstractClientDataHolder {
	default @NotNull CPAOtherClientData cpa$getCPAData() {
		throw new AssertionError("AdvCPAOtherClientDataHolder default method called?!");
	}
}
