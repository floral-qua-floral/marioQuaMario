package com.fqf.charapoweract.cpadata.injections;

import com.fqf.charapoweract.cpadata.CPAServerPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvCPAServerDataHolder extends AdvCPADataHolder {
	default @NotNull CPAServerPlayerData cpa$getCPAData() {
		throw new AssertionError("AdvCPAServerDataHolder default method called?!");
	};
}
