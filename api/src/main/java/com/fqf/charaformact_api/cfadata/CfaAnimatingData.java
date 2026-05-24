package com.fqf.charaformact_api.cfadata;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.HandPreference;

public interface CfaAnimatingData extends CfaReadableMotionData, CfaClientData {
	HandPreference getCurrentHandPreference();
	float getRelativeHeadYawRadians();
	float getRelativeHeadYawDegrees();
}
