package com.fqf.mario_qua_mario.util;

public interface Powers {
	// Powers for all Mario-series characters
	String DROP_COINS = "Drop coins";
	String LIGHTNING_SHRINKS = "Lightning causes Mini form";
	String CEILING_CLIPPING = "Clips past ceiling edges on ascent";
	String STOMP_GUARD = "Protected from underfoot melee attacks";

	// Power for Mr. Mario Mario himself
	String SLEEPY = "Falls asleep while idle";

	// Power for Toads
	String MYCOLOGICAL = "Mycological Nature";
	String USES_TOAD_CUSTOMIZATIONS = "Uses Toad Customizations";

	// Powers associated with Raccoon and Tanooki forms
	String SMB3_IDLE = "SMB3 Idle";
	String TAIL_ATTACK = "Tail Attack";
	String TAIL_STALL = "Tail Stall";
	String TAIL_FLY = "Tail Fly";

	// Powers associated with Raccoon, Tanooki, and Cape forms
	String CAN_HIT_PROJECTILES = "Can Hit Projectiles With Crosshair";

	// Powers associated with Raccoon and Elephant forms. Specific to those which affect Mario's body directly, so
	// costume-based forms like Tanooki Mario and Penguin Mario would not have this power. Cat Mario is a weird case
	// because it's a costume, but it also alters Mario's posture in a way that implies physical transformation beneath
	// the suit?? I think I'd give Cat Mario this power, since Cat Form has never been acquired from a Suit power-up,
	// and also because of Meowser's design showing that the Super Bell does do bodily transformation at least some of
	// the time.
	String TAPETUM_LUCIDUM = "Tapetum Lucidum";

	// Powers associated with Mini form
	String SPRINT_ON_WATER = "Can sprint on water";
	double SPRINT_ON_WATER_THRESHOLD = 0.14;
	String TALLER_SOLID_WATER_HITBOX = "Use taller hitbox for solid fluids";

	// Powers associated with Mini and Penguin forms
	String SNOW_SHOES = "Never sink into snow";
}
