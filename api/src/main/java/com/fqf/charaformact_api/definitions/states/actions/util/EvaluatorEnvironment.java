package com.fqf.charaformact_api.definitions.states.actions.util;

public enum EvaluatorEnvironment {
	CLIENT_ONLY(true, false),
	SERVER_ONLY(false, true),
	COMMON(true, true),
	CLIENT_CHECKED(true, false);

	public final boolean CHECK_ON_CLIENT, CHECK_ON_SERVER, IS_NETWORKED;
	EvaluatorEnvironment(boolean checkOnClient, boolean checkOnServer) {
		this.CHECK_ON_CLIENT = checkOnClient;
		this.CHECK_ON_SERVER = checkOnServer;
		this.IS_NETWORKED = !checkOnClient || !checkOnServer;
	}
}
