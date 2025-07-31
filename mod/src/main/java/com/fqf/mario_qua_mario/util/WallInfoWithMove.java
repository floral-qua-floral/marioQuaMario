package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;

public interface WallInfoWithMove extends WallboundActionDefinition.WallInfo {
	void setTowardsWallVel(double velocity);
	void setSidleVel(double velocity);
}
