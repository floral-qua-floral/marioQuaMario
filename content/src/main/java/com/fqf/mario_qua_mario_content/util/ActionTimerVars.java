package com.fqf.mario_qua_mario_content.util;

import com.fqf.charapoweract_api.cpadata.ICPAData;

public class ActionTimerVars {
	public int actionTimer;

	public static ActionTimerVars get(ICPAData data) {
		return data.retrieveStateData(ActionTimerVars.class);
	}
}
