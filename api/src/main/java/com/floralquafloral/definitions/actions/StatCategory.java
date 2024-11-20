package com.floralquafloral.definitions.actions;

public enum StatCategory {
	WALKING,
	RUNNING,
	P_RUNNING,
	DUCKING,

	DRIFTING, // Airborne

	SWIMMING, // Aquatic

	ACCELERATION,
	OVERSPEED_CORRECTION, // Used INSTEAD OF acceleration for overwalk, overrun, etc. type stats.
	SPEED,
	REDIRECTION,
	THRESHOLD,

	FORWARD,
	BACKWARD,
	STRAFE,

	DRAG,
	FRICTION, // Mostly for Luigi?

	JUMP_VELOCITY,
	JUMP_CAP,

	JUMPING_GRAVITY,
	NORMAL_GRAVITY,
	TERMINAL_VELOCITY,

	STOMP_BASE_DAMAGE,
	STOMP_ARMOR_MULTIPLIER,
	STOMP_BOUNCE
}
