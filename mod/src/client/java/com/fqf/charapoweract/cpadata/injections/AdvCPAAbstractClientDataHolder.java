package com.fqf.charapoweract.cpadata.injections;

import com.fqf.charapoweract.cpadata.MarioAnimationData;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import org.jetbrains.annotations.NotNull;

public interface AdvCPAAbstractClientDataHolder extends AdvCPADataHolder {
	default @NotNull CPAPlayerData cpa$getCPAData() {
		throw new AssertionError("AdvCPAAbstractClientDataHolder default method called?!");
	}

	default @NotNull MarioAnimationData mqm$getAnimationData() {
		throw new AssertionError("AdvCPAAbstractClientDataHolder default method (animation) called?!");
	}
}
