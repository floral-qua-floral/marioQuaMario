package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MountedActionDefinition extends IncompleteActionDefinition {
	@Nullable MutableText dismountingHint();

	boolean travelHook(CfaTravelData data, Entity mount, MountedActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(MountedActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(MountedActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(MountedActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Mounted Actions. Can be cast to any of the
	 * other ActionHelpers, if for whatever reason you need them.
	 */
	interface MountedActionHelper {
		Entity getMount(CfaReadableMotionData data);

		void dismount(CfaTravelData data, boolean reposition);

		double getSlipFactor(Entity mount);
	}
}
