package com.fqf.charapoweract_api.cpadata;

import com.fqf.charapoweract_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charapoweract_api.util.CharaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface ICPAData {
	PlayerEntity getPlayer();
	boolean isClient();
	default boolean isServer() {
		return !this.isClient();
	}

	boolean isEnabled();
	Identifier getActionID();
	ActionCategory getActionCategory();
	Identifier getPowerFormID();
	int getPowerFormValue();
	Identifier getCharacterID();

	boolean hasPower(String power);

	double getStat(CharaStat stat);
	float getHorizontalScale();
	float getVerticalScale();
	float getEyeHeightScale();
	int getBapStrength(Direction direction);

	<T> T retrieveStateData(Class<T> clazz);

	void forceBodyAlignment(boolean urgent);

	double getImmersionLevel();
	double getImmersionPercent();

	boolean isOnGround();
	boolean isNearGround(double range);
	double getSolidDistance(double maxDistance, Direction direction);
}
