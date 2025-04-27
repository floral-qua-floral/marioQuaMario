package com.fqf.mario_qua_mario_api.definitions.states.actions;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MountedActionDefinition extends IncompleteActionDefinition {
	@Nullable MutableText dismountingHint();

	boolean travelHook(IMarioTravelData data, Entity mount, MountedActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(MountedActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(MountedActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(MountedActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Mounted Actions. Can be cast to any of the
	 * other ActionHelpers, if for whatever reason you need them.
	 */
	interface MountedActionHelper {
		Entity getMount(IMarioReadableMotionData data);

		void dismount(IMarioTravelData data, boolean reposition);

		double getSlipFactor(Entity mount);
	}
}
