package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class MarioContentSFX {
	public static final SoundEvent DUCK = makeActionSound("duck");
	public static final SoundEvent GROUND_POUND_FLIP = makeActionSound("ground_pound_flip");
	public static final SoundEvent UNDUCK = makeActionSound("unduck");

	public static final SoundEvent FLIP = makeMovementSound("flip");
	public static final SoundEvent MARIO_JUMP = makeMovementSound("mario_jump");
	public static final SoundEvent LUIGI_JUMP = makeMovementSound("luigi_jump");

	public static final SoundEvent FIREBALL = makePowerUpSound("fireball");
	public static final SoundEvent FIREBALL_ENEMY = makePowerUpSound("fireball_enemy");
	public static final SoundEvent FIREBALL_WALL = makePowerUpSound("fireball_wall");
	public static final SoundEvent TAIL_EMPOWER = makePowerUpSound("tail_empower");
	public static final SoundEvent TAIL_FLY = makePowerUpSound("tail_fly");
	public static final SoundEvent TAIL_WHIP = makePowerUpSound("tail_whip");

	public static final SoundEvent HEAVY = makeStompSound("heavy");
	public static final SoundEvent KICK = makeStompSound("kick");
	public static final SoundEvent SPIN = makeStompSound("spin");
	public static final SoundEvent STOMP = makeStompSound("stomp");
	public static final SoundEvent YOSHI = makeStompSound("yoshi");

	private static SoundEvent makeMovementSound(String name) {
		return makeAndRegisterSound("sfx.movement." + name);
	}
	private static SoundEvent makePowerUpSound(String name) {
		return makeAndRegisterSound("sfx.power_up." + name);
	}
	private static SoundEvent makeStompSound(String name) {
		return makeAndRegisterSound("sfx.stomp." + name);
	}
	private static SoundEvent makeActionSound(String name) {
		return makeAndRegisterSound("sfx.action." + name);
	}

	private static SoundEvent makeAndRegisterSound(String path) {
		Identifier identifier = MarioQuaMarioContent.makeResID(path);
		SoundEvent event = SoundEvent.of(identifier);

		Registry.register(Registries.SOUND_EVENT, identifier, event);

		return event;
	}
	public static void staticInitialize() {

	}
}
