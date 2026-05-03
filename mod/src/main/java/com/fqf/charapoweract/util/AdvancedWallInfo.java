package com.fqf.charapoweract.util;

import com.fqf.charapoweract_api.definitions.states.actions.WallboundActionDefinition;

public interface AdvancedWallInfo extends WallboundActionDefinition.WallInfo {
	void setTowardsWallVel(double velocity);
	void setSidleVel(double velocity);

	void setYaw(float yaw);
}
