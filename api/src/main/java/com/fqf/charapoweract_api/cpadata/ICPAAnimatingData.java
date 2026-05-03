package com.fqf.charapoweract_api.cpadata;

import com.fqf.charapoweract_api.definitions.states.actions.util.animation.HandPreference;

public interface ICPAAnimatingData extends ICPAReadableMotionData, ICPAClientData {
	HandPreference getCurrentHandPreference();
	float getRelativeHeadYaw();
}
