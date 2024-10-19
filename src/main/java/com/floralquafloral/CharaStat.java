package com.floralquafloral;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import net.minecraft.world.World;

public enum CharaStat {
	ALL_JUMP_VELOCITIES,
	ALL_GRAVITIES,
	ALL_FRICTIONS,

	WALK_ACCEL(0.045),
	WALK_STANDSTILL_ACCEL(0.125, ALL_FRICTIONS),
	WALK_STANDSTILL_THRESHOLD(0.12),
	WALK_SPEED(0.275),
	WALK_REDIRECTION(0),
	OVERWALK_ACCEL(0.02),

	IDLE_DEACCEL(0.075, ALL_FRICTIONS),

	BACKPEDAL_ACCEL(0.055),
	BACKPEDAL_SPEED(0.225),
	BACKPEDAL_REDIRECTION(0),
	OVERBACKPEDAL_ACCEL(0.04),
	UNDERBACKPEDAL_ACCEL(0.055, ALL_FRICTIONS),

	DUCK_SLIDE_THRESHOLD(0.25),
	DUCK_SLIDE_BOOST(-0.15),
	DUCK_SLIDE_DRAG(0.03, ALL_FRICTIONS),
	DUCK_SLIDE_DRAG_MIN(0.01, ALL_FRICTIONS),
	DUCK_SLIDE_REDIRECTION(4),

	WADDLE_ACCEL(0.06),
	WADDLE_SPEED(0.08),
	WADDLE_STRAFE_ACCEL(0.06),
	WADDLE_STRAFE_SPEED(0.06),
	WADDLE_BACKPEDAL_ACCEL(0.0725),
	WADDLE_BACKPEDAL_SPEED(0.06),
	WADDLE_REDIRECTION(0),

	DUCK_JUMP_VELOCITY(0.858, ALL_JUMP_VELOCITIES),
	DUCK_JUMP_CAP(0.14),

	SKID_THRESHOLD(0.285),
	SKID_DRAG(0.185, ALL_FRICTIONS),
	SKID_DRAG_MIN(0.02, ALL_FRICTIONS),
	SKID_REDIRECTION(4.5),

	RUN_ACCEL(0.0155),
	RUN_SPEED(0.5),
	RUN_REDIRECTION(2.75),
	OVERRUN_ACCEL(0.0175),

	P_SPEED(1.0),
	P_SPEED_ACCEL(0.13),
	P_SPEED_REDIRECTION(6.0),

	STRAFE_ACCEL(0.065),
	STRAFE_SPEED(0.275),

	SKATE_SPEED(0.5),
	SKATE_REDIRECTION(1.2),

	GRAVITY(-0.115, ALL_GRAVITIES),
	JUMP_GRAVITY(-0.095, ALL_GRAVITIES),
	TERMINAL_VELOCITY(-3.25),

	DRIFT_FORWARD_ACCEL(0.04),
	DRIFT_FORWARD_SPEED(WALK_SPEED.getDefaultValue()),
	DRIFT_SIDE_ACCEL(0.04),
	DRIFT_SIDE_SPEED(0.2),
	DRIFT_BACKWARD_ACCEL(0.05),
	DRIFT_BACKWARD_SPEED(0.2),
	DRIFT_REDIRECTION(6.0),

	JUMP_SPEED_LOSS(0.075),
	JUMP_VELOCITY(0.858, ALL_JUMP_VELOCITIES),
	JUMP_VELOCITY_ADDEND(0.117, ALL_JUMP_VELOCITIES),
	JUMP_CAP(0.39),

	BACKFLIP_VELOCITY(1.065, ALL_JUMP_VELOCITIES),
	BACKFLIP_CAP(0.765),
	BACKFLIP_BACKWARD_SPEED(-0.375),

	SIDEFLIP_VELOCITY(1.065, ALL_JUMP_VELOCITIES),
	SIDEFLIP_CAP(0.65),
	SIDEFLIP_THRESHOLD(0.2),
	SIDEFLIP_BACKWARD_SPEED(-0.375),

	ADVANCED_JUMP_THRESHOLD(0.34),

	DOUBLE_JUMP_VELOCITY(0.939, ALL_JUMP_VELOCITIES),
	DOUBLE_JUMP_VELOCITY_ADDEND(0.08, ALL_JUMP_VELOCITIES),
	DOUBLE_JUMP_CAP(0.285),

	TRIPLE_JUMP_VELOCITY(1.175, ALL_JUMP_VELOCITIES),
	TRIPLE_JUMP_CAP(0.65),

	LONG_JUMP_VELOCITY(0.0, ALL_JUMP_VELOCITIES),
	LONG_JUMP_CAP,
	LONG_JUMP_SPEED_FACTOR,
	LONG_JUMP_SPEED_ADDEND,
	LONG_JUMP_SPEED_CAP,

	LAVA_BOOST_VEL(1.5),
	LAVA_BOOST_RISING_DRIFT_ACCEL(0.0075),
	LAVA_BOOST_FALLING_DRIFT_ACCEL(0.055),
	LAVA_BOOST_DRIFT_SPEED(0.5),
	LAVA_BOOST_REDIRECTION(9.0),

	STOMP_BASE_DAMAGE(4),
	STOMP_BASE_VELOCITY(0.95, ALL_JUMP_VELOCITIES),
	STOMP_CAP(0.39),

	GROUND_POUND_VELOCITY,

	WATER_GRAVITY(0.0, ALL_GRAVITIES),
	WATER_TERMINAL_VELOCITY,
	WATER_DRAG,

	SWIM_UP_VEL,
	PADDLE_TERMINAL_VELOCITY,

	SWIM_ACCEL,
	SWIM_SPEED,
	SWIM_STRAFE_ACCEL,
	SWIM_STRAFE_SPEED,
	SWIM_BACKPEDAL_ACCEL,
	SWIM_BACKPEDAL_SPEED,

	AQUATIC_GROUND_POUND_VELOCITY,
	AQUATIC_GROUND_POUND_DRAG;

	private final double DEFAULT_VALUE;
	private final CharaStat PARENT_STAT;

	CharaStat() {
		this.DEFAULT_VALUE = 1.0;
		this.PARENT_STAT = null;
	}
	CharaStat(double defaultValue) {
		this.DEFAULT_VALUE = defaultValue;
		this.PARENT_STAT = null;
	}
	CharaStat(double defaultValue, CharaStat parentStat) {
		this.DEFAULT_VALUE = defaultValue;
		this.PARENT_STAT = parentStat;
	}

	public double getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public double get(MarioData data) {
		return this.getDefaultValue() * this.getMultiplier(data);
	}
	public double getAsThreshold(MarioData data) {
		return this.get(data) * 0.99;
	}
	public double getAsLimit(MarioData data) {
		return this.get(data) * 1.015;
	}

	public double getMultiplier(MarioData data) {
		World marioWorld = data.getMario().getWorld();
		boolean useCharacterStats = marioWorld.isClient ? MarioQuaMarioClient.useCharacterStats : marioWorld.getGameRules().getBoolean(MarioQuaMario.USE_CHARACTER_STATS);
		return (useCharacterStats ? 1.0 : 1.0) * 1.0;
	}

//	public double getValue(PlayerEntity player) {
//		if(ModMarioQuaMario.playerIsMarioClient(player)) return this.getValue();
//		return this.getValue(ModMarioQuaMario.getUseCharacterStats(player), ModMarioQuaMario.getCharacter(player), ModMarioQuaMario.getPowerUp(player));
//	}
//
//	public double getValue(boolean useCharacterStats, @NotNull MarioCharacter character, @NotNull PowerUp powerUp) {
//		return(this.getDefaultValue() * this.getMultiplier(useCharacterStats, character, powerUp));
//	}
//
//	public double getMultiplier(boolean useCharacterStats, @NotNull MarioCharacter character, @NotNull PowerUp powerUp) {
//		return(
//				(this.PARENT_STAT == null ? 1.0 : this.PARENT_STAT.getMultiplier(useCharacterStats, character, powerUp)) *
//				(useCharacterStats ? character.getStatFactor(this) : 1) *
//				(powerUp instanceof StatChangingPowerUp statChangingPowerUp ? statChangingPowerUp.getStatFactor(this) : 1.0)
//		);
//	}
}
