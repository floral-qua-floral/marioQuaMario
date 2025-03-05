package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.definitions.VoicelineSetDefinition;

import java.util.Set;

public class Voicelines implements VoicelineSetDefinition {
	public static final String SELECT = "select";

	public static final String BONK = "bonk";
	public static final String BURNT = "burnt";
	public static final String SHOCK = "shock";
	public static final String DEATH = "death";

	public static final String DUCK = "duck";
	public static final String DUCK_JUMP = "duck_jump";
	public static final String LONG_JUMP = "long_jump";
	public static final String BACKFLIP = "backflip";
	public static final String SIDEFLIP = "sideflip";
	public static final String WALL_JUMP = "wall_jump";
	public static final String DOUBLE_JUMP = "double_jump";
	public static final String TRIPLE_JUMP = "triple_jump";
	public static final String GYMNAST_SALUTE = "gymnast_salute";

	public static final String REVERT = "revert";
	public static final String FIREBALL = "fireball";
	public static final String TAIL_WHIP = "tail_whip";
	public static final String TAIL_SPIN = "tail_spin";
	public static final String GET_STAR = "get_star";

	@Override
	public Set<String> getVoiceLines() {
		return Set.of(
				SELECT,

				BONK,
				BURNT,
				SHOCK,
				DEATH,

				DUCK,
				DUCK_JUMP,
				LONG_JUMP,
				BACKFLIP,
				SIDEFLIP,
				WALL_JUMP,
				DOUBLE_JUMP,
				TRIPLE_JUMP,
				GYMNAST_SALUTE,

				REVERT,
				FIREBALL,
				TAIL_WHIP,
				TAIL_SPIN,
				GET_STAR
		);
	}
}
