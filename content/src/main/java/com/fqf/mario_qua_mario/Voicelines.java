package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.definitions.VoicelineSetDefinition;

import java.util.Set;

public class Voicelines implements VoicelineSetDefinition {
	@Override
	public Set<String> getVoiceLines() {
		return Set.of(
				"select",

				"bonk",
				"burnt",
				"shock",
				"death",

				"duck",
				"duck_jump",
				"long_jump",
				"backflip",
				"sideflip",
				"wall_jump",
				"double_jump",
				"triple_jump",
				"gymnast_salute",

				"revert",
				"fireball",
				"get_star"
		);
	}
}
