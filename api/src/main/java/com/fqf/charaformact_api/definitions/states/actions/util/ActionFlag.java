package com.fqf.charaformact_api.definitions.states.actions.util;

import java.util.Set;

public enum ActionFlag {
	DO_VANILLA_TRAVEL, // Causes vanilla movement code to run before the action's travelHook.

	NO_VIEW_BOB, // Prevents client-side view bobbing.
	NO_STEP, // Prevents sound effects and Sculk-detectable vibrations from footsteps.

	CLIMBING, // Determines result of vanilla isClimbing method.

	FLYING, // No effect.

	RESTORES_GROUNDED_RESOURCES, // Cancels fall damage and restores grounded resources. Always considered present on Grounded actions.

	DIVING, // Makes the player count as sprint-swimming. May affect certain modded mechanics.

	PROHIBIT_FLIGHT, // Prevents initiating Creative flight. Spectator mode is unaffected.

	PROHIBIT_SPRINT, // Cancels sprinting and does not allow it to start again.
	RESTRICT_SPRINT, // Prevents starting sprinting, but doesn't cancel sprinting if the player already was.
	OMNI_DIRECTIONAL_SPRINT, // Allows sprinting sideways and backwards, although not in place.
	ALLOW_SPRINT_AGAINST_WALL, // Prevents collisions against walls from disabling sprint.

	CROUCHING, // Makes the player count as being in the crouching pose. May affect certain modded mechanics.
	ALLOW_STEALTH, // Allows the player to hold Sneak to hide their nametag and silence their movement. No effect on motion.
	NO_LEDGE_CLIP; // Allows player to move off ledges while holding Sneak.

	public static final Set<ActionFlag> SLIDING = Set.of(NO_VIEW_BOB, NO_STEP);
	public static final Set<ActionFlag> SNEAKING = Set.of(CROUCHING, ALLOW_STEALTH, PROHIBIT_SPRINT);
	public static final Set<ActionFlag> FULL_CLIMBING = Set.of(CLIMBING, RESTORES_GROUNDED_RESOURCES, NO_LEDGE_CLIP,
			OMNI_DIRECTIONAL_SPRINT, ALLOW_SPRINT_AGAINST_WALL);
}
