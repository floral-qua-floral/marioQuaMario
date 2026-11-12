package com.fqf.charaformact_api.cfadata;

import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.util.CfaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.mutable.MutableInt;

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

	int getHealthBarCount();
	float getSingleHealthBarSize();
	float getHealthWithinForm();

	boolean hasPower(String power);

	double getStat(CfaStat stat);
	float getHorizontalScale();
	float getVerticalScale();
	float getEyeHeightScale();
	int getBapStrength(Direction direction);

	/// These methods are for handling State Attachment data. See [StateAttachmentKey] for details on this system.
	/// * [CfaData#get]: Retrieves the value for the provided key. If there is no value, the key's Default Assigner is
	/// used to set one, and then that value is returned.
	/// * [CfaData#has]: Checks whether any value is associated with the given key. If this returns false, then that
	/// means calling [CfaData#get] on this key would then run its Default Assigner.
	/// * [CfaData#put]: Can be used to manually replace the value associated with the provided key. This is usually
	/// unnecessary, as it is more convenient to make StateAttachmentKeys for storing mutable objects (for example,
	/// [MutableInt]), then retrieve and mutate that instance.
	/// * [CfaData#clear]: Removes the value associated with the given StateAttachmentKey. The next time it is accessed,
	/// it will use its Default Assigner. This is very similar to what happens automatically when the player's
	/// Character, Form, or Action change.
	/// * [CfaData#reset]: The same as [CfaData#clear], but it immediately triggers the Default Assigner right after.
	<T> T get(StateAttachmentKey<T> key);
	boolean has(StateAttachmentKey<?> key);
	<T> void put(StateAttachmentKey<T> key, T value);
	default <T> void clear(StateAttachmentKey<T> key) {
		this.put(key, null);
	}
	default <T> void reset(StateAttachmentKey<T> key) { this.clear(key); this.get(key); }

	void forceBodyAlignment(boolean urgent);

	double getImmersionLevel();
	double getImmersionPercent();

	boolean isOnGround();
	boolean isNearGround(double range);
	double getSolidDistance(double maxDistance, Direction direction);
}
