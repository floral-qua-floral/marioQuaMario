package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class MarioSFX {
	public static final SoundEvent BUMP = makeMovementSound("bump");
	public static final SoundEvent SKID = makeMovementSound("skid");
	public static final SoundEvent SKID_ICE = makeMovementSound("skid_ice");
	public static final SoundEvent SKID_SAND = makeMovementSound("skid_sand");
	public static final SoundEvent SKID_SNOW = makeMovementSound("skid_snow");
	public static final SoundEvent SKID_WALL = makeMovementSound("skid_wall");

	public static final SoundEvent EMPOWER = makePowerUpSound("empower");
	public static final SoundEvent REVERT = makePowerUpSound("revert");

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
		Identifier identifier = MarioQuaMario.makeResID(path);
		SoundEvent event = SoundEvent.of(identifier);

		Registry.register(Registries.SOUND_EVENT, identifier, event);

		return event;
	}
	public static void staticInitialize() {

	}
}
