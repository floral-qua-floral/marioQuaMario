package com.fqf.mario_qua_mario.definitions.actions.util;

import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.StatCategory;

public class BumpingRule {
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
	public static final BumpingRule JUMPING = new BumpingRule(4, 1);
	public static final BumpingRule FALLING = new BumpingRule(4, 1);
	public static final BumpingRule SWIMMING = new BumpingRule(4, 0);
	public static final BumpingRule GROUND_POUND = new BumpingRule(0, 4);
	public static final BumpingRule SPIN_JUMPING = new BumpingRule(2, 2);

	public final int CEILINGS;
	public final int FLOORS;
	public final int WALLS;
	public final CharaStat WALL_SPEED_THRESHOLD;

	public BumpingRule(int ceilingBumpStrength, int floorBumpStrength) {
		this(ceilingBumpStrength, floorBumpStrength, 0, 0);
	}

	public BumpingRule(int ceilingBumpStrength, int floorBumpStrength, int wallBumpStrength, double wallBumpSpeedThreshold) {
		this.CEILINGS = ceilingBumpStrength;
		this.FLOORS = floorBumpStrength;
		this.WALLS = wallBumpStrength;
		this.WALL_SPEED_THRESHOLD = new CharaStat(wallBumpSpeedThreshold, StatCategory.THRESHOLD);
	}
}
