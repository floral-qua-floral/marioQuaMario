package com.fqf.mario_qua_mario_content.util;

import com.fqf.charapoweract_api.mariodata.IMarioData;

public class ActionTimerVars {
	public int actionTimer;

	public static ActionTimerVars get(IMarioData data) {
		return data.getVars(ActionTimerVars.class);
	}
}
