package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.mariodata.IMarioData;

public class TailSpinActionTimerVars extends ActionTimerVars {
	public TailSpinActionTimerVars(IMarioData data) {
		TailSpinActionTimerVars oldVars = data.getVars(TailSpinActionTimerVars.class);
		if(oldVars != null) {
			this.actionTimer = oldVars.actionTimer;
		}
	}
}
