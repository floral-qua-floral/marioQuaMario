package com.fqf.mario_qua_mario.util;

import com.fqf.charaformact_api.cfadata.CfaData;

public class TailSpinActionTimerVars extends ActionTimerVars {
	public TailSpinActionTimerVars(CfaData data) {
		TailSpinActionTimerVars oldVars = data.retrieveStateData(TailSpinActionTimerVars.class);
		if(oldVars != null) {
			this.actionTimer = oldVars.actionTimer;
		}
	}
}
