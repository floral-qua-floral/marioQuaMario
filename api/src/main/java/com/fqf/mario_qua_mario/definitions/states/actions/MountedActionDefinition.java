package com.fqf.mario_qua_mario.definitions.states.actions;

import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MountedActionDefinition extends IncompleteActionDefinition {
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
