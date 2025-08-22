package com.fqf.mario_qua_mario_api.mariodata;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.HandPreference;

public interface IMarioAnimatingData extends IMarioReadableMotionData, IMarioClientData {
	HandPreference getCurrentHandPreference();
	float getRelativeHeadYaw();
}
