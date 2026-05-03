package com.fqf.mario_qua_mario_content.util;

import com.fqf.charapoweract_api.cpadata.ICPAData;

public class TailSpinActionTimerVars extends ActionTimerVars {
	public TailSpinActionTimerVars(ICPAData data) {
		TailSpinActionTimerVars oldVars = data.retrieveStateData(TailSpinActionTimerVars.class);
		if(oldVars != null) {
			this.actionTimer = oldVars.actionTimer;
		}
	}
}
