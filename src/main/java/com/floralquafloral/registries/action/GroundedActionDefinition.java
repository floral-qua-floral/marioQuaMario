package com.floralquafloral.registries.action;

public abstract class GroundedActionDefinition implements ActionDefinition {
	protected abstract static class GroundedTransitions {
		public static final ActionTransitionDefinition FALL = new ActionTransitionDefinition(
				"qua_mario:fall",
				(data) -> !data.MARIO.isOnGround()
		);
	}


}
