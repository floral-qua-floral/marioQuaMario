package com.fqf.charaformact_api.cfadata;

import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.util.CfaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface CfaData {
	PlayerEntity getPlayer();
	boolean isClient();
	default boolean isServer() {
		return !this.isClient();
	}

	boolean isEnabled();
	Identifier getActionID();
	ActionCategory getActionCategory();
	Identifier getFormID();
	int getFormPriority();
	Identifier getCharacterID();

	boolean hasPower(String power);

	double getStat(CfaStat stat);
	float getHorizontalScale();
	float getVerticalScale();
	float getEyeHeightScale();
	int getBapStrength(Direction direction);

	/**
	 * @param clazz The class of the state data you intend to retrieve. Returns null if none of the player's current
	 *              states have provided it via CfaStateDefinition.provideStateData. Unfortunately because of how this
	 *              works, you must provide the exact class of the stored state data; trying to get a parent or child
	 *              class will not find the data.
	 * @return The state data of the requested class.
	 */
	<T> T retrieveStateData(Class<T> clazz);

	void forceBodyAlignment(boolean urgent);

	double getImmersionLevel();
	double getImmersionPercent();

	boolean isOnGround();
	boolean isNearGround(double range);
	double getSolidDistance(double maxDistance, Direction direction);
}
