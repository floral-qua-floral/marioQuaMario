package com.fqf.charaformact_api.util;

public enum StatCategory {
	WALKING,
	RUNNING,
	P_RUNNING,

	DUCKING,

	DRIFTING, // Airborne

	SWIMMING, // Aquatic

	CLIMBING,

	ACCELERATION,
	OVERSPEED_CORRECTION, // Used INSTEAD OF acceleration for overwalk, overrun, etc. type stats.
	SPEED,
	REDIRECTION,
	THRESHOLD,

	FORWARD,
	BACKWARD,
	STRAFE,

	ASCEND, // Used for swimming and climbing.
	DESCEND, // Used for climbing and Ground Pounding.
	SIDLE, // Used for climbing side to side against a wall.

	DRAG,
	FRICTION, // Mostly for Luigi?
	WATER_DRAG,

	JUMP_VELOCITY,
	JUMP_CAP,

	RECOIL_VELOCITY,

	JUMPING_GRAVITY,
	NORMAL_GRAVITY,
	AQUATIC_GRAVITY,
	TERMINAL_VELOCITY,
	AQUATIC_TERMINAL_VELOCITY,

	COLLISION_ATTACK,
	FORM,

	DAMAGE
}
