package com.fqf.mario_qua_mario_api.mariodata;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.util.CharaStat;
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
	int getBapStrength(Direction direction);

	<T> T getVars(Class<T> clazz);

	void forceBodyAlignment(boolean urgent);

	double getImmersionLevel();
	double getImmersionPercent();

	boolean isOnGround();
	boolean isNearGround(double maxDistance);
	double getSolidDistance(double maxDistance, Direction direction);
}
