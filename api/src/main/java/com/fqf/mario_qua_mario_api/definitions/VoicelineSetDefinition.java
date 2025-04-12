package com.fqf.mario_qua_mario_api.definitions;

import java.util.Set;

/**
 * Provides a set of strings that will be used for Voicelines. A Voiceline is a group of sound events to represent
 * vocal sounds made by characters. As such, different characters will play different sound events when given the same
 * voiceline, and only one voiceline can play concurrently per player. If a player is already playing a voiceline sound
 * and starts another, the earlier one will be cut off. Voicelines will always be played under the VOICES sound category.
 */
public interface VoicelineSetDefinition {
	/**
	 * The sound events for a voiceline are under the namespace the character is registered under, with the path
	 * (character's path)_(voiceline name). For example, if you register a voiceline with the string "squished", when
	 * Mario (whose ID is "mqm:mario") says that line, it will play the sound event with key "mqm:mario_squished". If
	 * another mod adds a character like Sonic, with the ID "sonicmod:sonic", then Sonic would play
	 * "sonicmod:sonic_squished".
	 * <p>
	 * So if you add a voiceline and you want other mods' characters to be able to play it,
	 * you'll need to register the sound effects under those other mods' namespaces.
	 */
	Set<String> getVoiceLines();
}
