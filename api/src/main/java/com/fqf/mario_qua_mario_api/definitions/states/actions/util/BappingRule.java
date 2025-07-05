package com.fqf.mario_qua_mario_api.definitions.states.actions.util;

import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;

public record BappingRule(int ceilingBumpStrength, int floorBumpStrength, int wallBumpStrength, CharaStat wallBumpSpeedThreshold) {
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
	 * A strength of 1 represents Mario having no effect on a block, but still imparting some nonzero force.
	 * (Example: Regular jump)
	 * <p>
	 * A strength of 0 represents no special collision at all. This will never trigger even an attempted bap.
	 * (Example: Standing on a block)
	 */
	public static final BappingRule GROUNDED = new BappingRule(0, 0);
	public static final BappingRule JUMPING = new BappingRule(4, 1);
	public static final BappingRule FALLING = new BappingRule(4, 1);
	public static final BappingRule SWIMMING = new BappingRule(4, 0);
	public static final BappingRule GROUND_POUND = new BappingRule(0, 4);
	public static final BappingRule SPIN_JUMPING = new BappingRule(2, 2);

	public static final CharaStat DEFAULT_WALL_BUMP_SPEED_THRESHOLD = new CharaStat(0.3, StatCategory.THRESHOLD);

	public static final BappingRule ROLLING = new BappingRule(0, 0, 3, DEFAULT_WALL_BUMP_SPEED_THRESHOLD);

	public BappingRule(int ceilingBumpStrength, int floorBumpStrength) {
		this(ceilingBumpStrength, floorBumpStrength, 0, DEFAULT_WALL_BUMP_SPEED_THRESHOLD);
	}

	public BappingRule variate(Integer ceilingBumpStrength, Integer floorBumpStrength, Integer wallBumpStrength, CharaStat wallBumpSpeedThreshold) {
		return new BappingRule(
				ceilingBumpStrength == null ? this.ceilingBumpStrength : ceilingBumpStrength,
				floorBumpStrength == null ? this.floorBumpStrength : floorBumpStrength,
				wallBumpStrength == null ? this.wallBumpStrength : wallBumpStrength,
				wallBumpSpeedThreshold == null ? this.wallBumpSpeedThreshold : wallBumpSpeedThreshold
		);
	}
}
