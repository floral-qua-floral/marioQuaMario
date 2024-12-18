package com.fqf.mario_qua_mario.definitions.states.actions.util;

import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.StatCategory;

public record BumpType(int ceilingBumpStrength, int floorBumpStrength, int wallBumpStrength, CharaStat wallBumpSpeedThreshold) {
	/**
	 * A strength of 4 represents Super Mario being able to destroy a Brick Block, but Small Mario only bumping it.
	 * (Example: Ground Pound, hitting a block from below)
	 * <p>
	 * A strength of 3 represents Super Mario and Small Mario both bumping a Brick Block without destroying it.
	 * (Example: Rolling into a wall, Bonking)
	 * <p>
	 * A strength of 2 represents Super Mario being able to shatter a Flip Block, and Small Mario having no effect on it.
	 * (Example: Spin Jump)
	 * <p>
	 * A strength of 1 represents Mario landing on a block and having no effect on it.
	 * (Example: Regular jump)
	 */
	public static final BumpType GROUNDED = new BumpType(0, 0);
	public static final BumpType JUMPING = new BumpType(4, 1);
	public static final BumpType FALLING = new BumpType(4, 1);
	public static final BumpType SWIMMING = new BumpType(4, 0);
	public static final BumpType GROUND_POUND = new BumpType(0, 4);
	public static final BumpType SPIN_JUMPING = new BumpType(2, 2);

	public BumpType(int ceilingBumpStrength, int floorBumpStrength) {
		this(ceilingBumpStrength, floorBumpStrength, 0, new CharaStat(0, StatCategory.THRESHOLD));
	}
}
