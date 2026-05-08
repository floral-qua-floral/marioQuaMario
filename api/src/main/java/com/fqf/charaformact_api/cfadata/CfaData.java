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
	 * @param clazz The class of the state data you intend to retrieve. Data of this class MUST have already been
	 *              provided through CfaStateDefinition.provideStateData; consequences otherwise are unspecified!
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
