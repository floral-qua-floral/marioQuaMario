package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class MarioSFX {
	public static final SoundEvent AQUATIC_GROUND_POUND_DROP = makeActionSound("aquatic_ground_pound_drop");
	public static final SoundEvent AQUATIC_GROUND_POUND_FLIP = makeActionSound("aquatic_ground_pound_flip");
	public static final SoundEvent AQUATIC_GROUND_POUND_LAND = makeActionSound("aquatic_ground_pound_land");
	public static final SoundEvent BONK = makeActionSound("bonk");
	public static final SoundEvent DUCK = makeActionSound("duck");
	public static final SoundEvent GROUND_POUND_DROP = makeActionSound("ground_pound_drop");
	public static final SoundEvent GROUND_POUND_FLIP = makeActionSound("ground_pound_flip");
	public static final SoundEvent GROUND_POUND_LAND = makeActionSound("ground_pound_land");
	public static final SoundEvent SWIM = makeActionSound("swim");
	public static final SoundEvent SWIM_PADDLE = makeActionSound("swim_paddle");
	public static final SoundEvent UNDUCK = makeActionSound("unduck");

	public static final SoundEvent HARMLESS = makeCollisionAttackSound("harmless");
	public static final SoundEvent HEAVY = makeCollisionAttackSound("heavy");
	public static final SoundEvent KICK = makeCollisionAttackSound("kick");
	public static final SoundEvent LAST = makeCollisionAttackSound("last");
	public static final SoundEvent SPIN = makeCollisionAttackSound("spin");
	public static final SoundEvent STOMP = makeCollisionAttackSound("stomp");
	public static final SoundEvent YOSHI = makeCollisionAttackSound("yoshi");

	public static final SoundEvent BURN_OBJECT = makeFormSound("burn_object");
	public static final SoundEvent COIN = makeFormSound("coin");
	public static final SoundEvent COIN_USE = makeFormSound("coin_use");
	public static final SoundEvent FIREBALL = makeFormSound("fireball");
	public static final SoundEvent FIREBALL_ENEMY = makeFormSound("fireball_enemy");
	public static final SoundEvent FIREBALL_WALL = makeFormSound("fireball_wall");
	public static final SoundEvent TAIL_EMPOWER = makeFormSound("tail_empower");
	public static final SoundEvent TAIL_FLY = makeFormSound("tail_fly");
	public static final SoundEvent TAIL_WHIP = makeFormSound("tail_whip");

	public static final SoundEvent FLIP = makeMovementSound("flip");
	public static final SoundEvent MARIO_JUMP = makeMovementSound("mario_jump");
	public static final SoundEvent LUIGI_JUMP = makeMovementSound("luigi_jump");
	public static final SoundEvent TOAD_JUMP = makeMovementSound("toad_jump");
	public static final SoundEvent TOADETTE_JUMP = makeMovementSound("toadette_jump");
	public static final SoundEvent WALL_JUMP = makeMovementSound("wall_jump");

	private static SoundEvent makeMovementSound(String name) {
		return makeAndRegisterSound("sfx.movement." + name);
	}
	private static SoundEvent makeFormSound(String name) {
		return makeAndRegisterSound("sfx.form." + name);
	}
	private static SoundEvent makeCollisionAttackSound(String name) {
		return makeAndRegisterSound("sfx.collision_attack." + name);
	}
	private static SoundEvent makeActionSound(String name) {
		return makeAndRegisterSound("sfx.action." + name);
	}

	private static SoundEvent makeAndRegisterSound(String path) {
		Identifier identifier = MarioQuaMario.makeID(path);
		SoundEvent event = SoundEvent.of(identifier);

		Registry.register(Registries.SOUND_EVENT, identifier, event);

		return event;
	}
	public static void staticInitialize() {

	}
}
