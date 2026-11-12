package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.google.common.collect.ImmutableSet;

public interface CfaStateDefinition {
	default void commonTick(CfaData data) {

	}
	default void clientTick(CfaClientData data, boolean isSelf) {

	}
	default void serverTick(CfaAuthoritativeData data) {

	}

	default void onEnter(CfaData data, ImmutableSet.Builder<StateAttachmentKey<?>> attachmentsToKeep) {

	}
	default void onExit(CfaData data) {

	}
}
