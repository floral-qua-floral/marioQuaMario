package com.fqf.mario_qua_mario.util;

import com.fqf.charaformact_api.cfadata.CfaData;

public class ActionTimerVars {
	public int actionTimer;

	public static ActionTimerVars get(CfaData data) {
		return data.retrieveStateData(ActionTimerVars.class);
	}
}
