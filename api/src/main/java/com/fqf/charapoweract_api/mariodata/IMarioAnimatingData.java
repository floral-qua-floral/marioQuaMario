package com.fqf.charapoweract_api.mariodata;

import com.fqf.charapoweract_api.definitions.states.actions.util.animation.HandPreference;

public interface IMarioAnimatingData extends IMarioReadableMotionData, IMarioClientData {
	HandPreference getCurrentHandPreference();
	float getRelativeHeadYaw();
}
