package com.floralquafloral.util;

import com.floralquafloral.MarioQuaMario;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public abstract class MarioSFX {
	public static final SoundEvent JUMP = makeMovementSound("jump");
	public static final SoundEvent FLIP = makeMovementSound("flip");
	public static final SoundEvent SKID_BLOCK = makeMovementSound("skid_block");
	public static final SoundEvent SKID_ICE = makeMovementSound("skid_ice");
	public static final SoundEvent SKID_SAND = makeMovementSound("skid_sand");
	public static final SoundEvent SKID_SNOW = makeMovementSound("skid_snow");
	public static final SoundEvent SKID_WALL = makeMovementSound("skid_wall");

	public static final SoundEvent NORMAL_POWER = makePowerSound("normal_power");
	public static final SoundEvent REVERT = makePowerSound("revert");

	public static final SoundEvent STOMP = makeStompSound("stomp");
	public static final SoundEvent STOMP_POINTY = makeStompSound("pointy");
	public static final SoundEvent STOMP_SPIN = makeStompSound("spin");
	public static final SoundEvent STOMP_HEAVY = makeStompSound("heavy");
	public static final SoundEvent STOMP_YOSHI = makeStompSound("yoshi");

	public static final SoundEvent DUCK = makeAndRegisterSound("duck");

	private static SoundEvent makeMovementSound(String name) {
		return makeAndRegisterSound("sfx.movement." + name);
	}
	private static SoundEvent makePowerSound(String name) {
		return makeAndRegisterSound("sfx.power_up." + name);
	}
	private static SoundEvent makeStompSound(String name) {
		return makeAndRegisterSound("sfx.stomp." + name);
	}
	private static SoundEvent makeActionSound(String name) {
		return makeAndRegisterSound("sfx.action." + name);
	}

	private static SoundEvent makeAndRegisterSound(String path) {
		Identifier identifier = Identifier.of(MarioQuaMario.MOD_ID, path);
		SoundEvent event = SoundEvent.of(identifier);
		MarioQuaMario.LOGGER.info("Register sound {} ({})", identifier, event);

		Registry.register(Registries.SOUND_EVENT, identifier, event);

		return event;
	}
	public static void staticInitialize() {

	}
}
