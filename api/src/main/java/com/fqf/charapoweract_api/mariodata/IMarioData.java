package com.fqf.charapoweract_api.mariodata;

import com.fqf.charapoweract_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charapoweract_api.util.CharaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface IMarioData {
	PlayerEntity getMario();
	boolean isClient();
	default boolean isServer() {
		return !this.isClient();
	}

	boolean isEnabled();
	Identifier getActionID();
	ActionCategory getActionCategory();
	Identifier getPowerUpID();
	int getPowerUpValue();
	Identifier getCharacterID();

	boolean hasPower(String power);

	double getStat(CharaStat stat);
	float getHorizontalScale();
	float getVerticalScale();
	float getEyeHeightScale();
	int getBapStrength(Direction direction);

	<T> T getVars(Class<T> clazz);

	void forceBodyAlignment(boolean urgent);

	double getImmersionLevel();
	double getImmersionPercent();

	boolean isOnGround();
	boolean isNearGround(double range);
	double getSolidDistance(double maxDistance, Direction direction);
}
