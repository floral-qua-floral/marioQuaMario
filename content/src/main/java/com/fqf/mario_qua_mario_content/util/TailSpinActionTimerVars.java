package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.mariodata.IMarioData;

public class TailSpinActionTimerVars extends ActionTimerVars {
	public TailSpinActionTimerVars(IMarioData data) {
		TailSpinActionTimerVars oldVars = data.getVars(TailSpinActionTimerVars.class);
		if(oldVars != null) {
			this.actionTimer = oldVars.actionTimer;
		}
	}
}
