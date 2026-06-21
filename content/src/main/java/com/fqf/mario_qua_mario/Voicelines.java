package com.fqf.mario_qua_mario;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;

import java.util.Set;

import static com.fqf.mario_qua_mario.MarioQuaMario.makeID;

public interface Voicelines {
	Identifier BONK = makeID("bonk");
	Identifier BURNT = makeID("burnt");

	Identifier DUCK = makeID("duck");
	Identifier DUCK_JUMP = makeID("duck_jump");
	Identifier LONG_JUMP = makeID("long_jump");
	Identifier BACKFLIP = makeID("backflip");
	Identifier SIDEFLIP = makeID("sideflip");
	Identifier WALL_JUMP = makeID("wall_jump");
	Identifier DOUBLE_JUMP = makeID("double_jump");
	Identifier TRIPLE_JUMP = makeID("triple_jump");
	Identifier GYMNAST_SALUTE = makeID("gymnast_salute");

	Identifier FIREBALL = makeID("fireball");
	Identifier TAIL_WHIP = makeID("tail_whip");
	Identifier TAIL_SPIN = makeID("tail_spin");

	static void addAll(ImmutableSet.Builder<Identifier> builder) {
		builder.add(
				BONK,
				BURNT,

				DUCK,
				DUCK_JUMP,
				LONG_JUMP,
				BACKFLIP,
				SIDEFLIP,
				WALL_JUMP,
				DOUBLE_JUMP,
				TRIPLE_JUMP,
				GYMNAST_SALUTE,

				FIREBALL,
				TAIL_WHIP,
				TAIL_SPIN
		);
	}
}
