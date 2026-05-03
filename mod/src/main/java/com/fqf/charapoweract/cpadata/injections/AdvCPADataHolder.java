package com.fqf.charapoweract.cpadata.injections;

import com.fqf.charapoweract.cpadata.CPAPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvCPADataHolder {
	default @NotNull CPAPlayerData cpa$getCPAData() {
		throw new AssertionError("AdvCPADataHolder default method called?!");
	};
	default void cpa$setCPAData(CPAPlayerData replacementData) {
		throw new AssertionError("AdvCPADataHolder default method called?!");
	};
}
