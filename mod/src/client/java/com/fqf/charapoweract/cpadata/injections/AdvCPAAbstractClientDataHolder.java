package com.fqf.charapoweract.cpadata.injections;

import com.fqf.charapoweract.cpadata.CPAAnimationData;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvCPAAbstractClientDataHolder extends AdvCPADataHolder {
	default @NotNull CPAPlayerData cpa$getCPAData() {
		throw new AssertionError("AdvCPAAbstractClientDataHolder default method called?!");
	}

	default @NotNull CPAAnimationData cpa$getAnimationData() {
		throw new AssertionError("AdvCPAAbstractClientDataHolder default method (animation) called?!");
	}
}
