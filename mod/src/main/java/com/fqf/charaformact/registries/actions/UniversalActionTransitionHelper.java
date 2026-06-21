package com.fqf.charaformact.registries.actions;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.StatCategory;
import net.minecraft.util.Identifier;

public class UniversalActionTransitionHelper extends UniversalActionDefinitionHelper {
	private final AbstractParsedAction FOR_ACTION;

	public UniversalActionTransitionHelper(AbstractParsedAction forAction) {
		this.FOR_ACTION = forAction;
	}

	@Override
	public ActionTransitionDetails makeJumpCapTransition(double capThreshold) {
		return makeJumpCapTransition(this.FOR_ACTION.ID, capThreshold);
	}
}
