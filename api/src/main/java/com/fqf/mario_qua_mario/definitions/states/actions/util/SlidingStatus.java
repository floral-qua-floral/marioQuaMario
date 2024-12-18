package com.fqf.mario_qua_mario.definitions.states.actions.util;

/**
 * Affects whether Mario plays footstep sounds and whether view-bobbing occurs.
 * This does have a server-side effect - if footsteps are suppressed, Sculk Sensors won't be triggered!
 * <p>NOT_SLIDING: Vanilla behavior
 * <p>NOT_SLIDING_SMOOTH: Footsteps occur, but no view-bobbing.
 * <p>
 * <p>SLIDING: No footsteps or view-bobbing. Sound effect is highly dependent on speed.
 * <p>SKIDDING: No footsteps or view-bobbing. Sound effect mostly ignores speed.
 * <p>WALL_SLIDING: No footsteps or view-bobbing. Plays a different sound effect, which completely ignores
 * horizontal speed.
 * <p>
 * <p>SLIDING_SILENT: No footsteps or view-bobbing. No sound effect.
 */
public enum SlidingStatus {
	NOT_SLIDING,
	NOT_SLIDING_SMOOTH,

	SLIDING,
	SKIDDING,
	WALL_SLIDING,

	SLIDING_SILENT
}
