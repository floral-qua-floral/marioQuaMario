package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.registries.ParsedAttackInterception;
import com.fqf.mario_qua_mario.registries.ParsedMarioThing;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ParsedPowerUp extends ParsedPowerGrantingState {
	public final Identifier REVERSION_TARGET_ID;
	public final int VALUE;

	public final SoundEvent ACQUISITION_SOUND;
	public final float VOICE_PITCH;

	public final PowerUpDefinition.PowerHeart HEART;

	public final List<ParsedAttackInterception> INTERCEPTIONS;

	public ParsedPowerUp(PowerUpDefinition definition) {
		super(definition);

		this.REVERSION_TARGET_ID = definition.getReversionTarget();
		this.VALUE = definition.getValue();

		SoundEvent definedAcquisitionSound = definition.getAcquisitionSound();
		this.ACQUISITION_SOUND = definedAcquisitionSound == null ? SoundEvents.BLOCK_SHROOMLIGHT_PLACE : definedAcquisitionSound;
		this.VOICE_PITCH = definition.getVoicePitch();

		this.HEART = definition.getPowerHeart(new PowerHeartHelperImpl(this.ID));

		this.INTERCEPTIONS = new ArrayList<>();
		for (AttackInterceptingStateDefinition.AttackInterceptionDefinition interception : definition.getAttackInterceptions()) {
			this.INTERCEPTIONS.add(new ParsedAttackInterception(interception, false));
		}
	}
}
