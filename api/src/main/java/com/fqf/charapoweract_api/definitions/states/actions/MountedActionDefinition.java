package com.fqf.charapoweract_api.definitions.states.actions;

import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.cpadata.ICPAReadableMotionData;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MountedActionDefinition extends IncompleteActionDefinition {
	@Nullable MutableText dismountingHint();

	boolean travelHook(ICPATravelData data, Entity mount, MountedActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(MountedActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(MountedActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(MountedActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Mounted Actions. Can be cast to any of the
	 * other ActionHelpers, if for whatever reason you need them.
	 */
	interface MountedActionHelper {
		Entity getMount(ICPAReadableMotionData data);

		void dismount(ICPATravelData data, boolean reposition);

		double getSlipFactor(Entity mount);
	}
}
