package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.registries.ParsedAttackInterception;
import com.fqf.mario_qua_mario.registries.actions.AnimationHelperImpl;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ParsedPowerUp extends ParsedPowerGrantingState {
	public final Identifier REVERSION_TARGET_ID;
	public final int VALUE;

	public final SoundEvent ACQUISITION_SOUND;
	public final float VOICE_PITCH;
	public final float JUMP_PITCH;

	public final PowerUpDefinition.PowerHeart HEART;

	public final List<ParsedAttackInterception> INTERCEPTIONS;

	public ParsedPowerUp(PowerUpDefinition definition) {
		super(definition);

		this.REVERSION_TARGET_ID = definition.getReversionTarget();
		this.VALUE = definition.getValue();

		SoundEvent definedAcquisitionSound = definition.getAcquisitionSound();
		this.ACQUISITION_SOUND = definedAcquisitionSound == null ? MarioSFX.EMPOWER : definedAcquisitionSound;
		this.VOICE_PITCH = definition.getVoicePitch();
		this.JUMP_PITCH = definition.getJumpPitch();

		this.HEART = definition.getPowerHeart(new PowerHeartHelperImpl(this.RESOURCE_ID));

		this.INTERCEPTIONS = new ArrayList<>();
		for (AttackInterceptingStateDefinition.AttackInterceptionDefinition interception : definition.getAttackInterceptions(AnimationHelperImpl.INSTANCE)) {
			this.INTERCEPTIONS.add(new ParsedAttackInterception(interception, false));
		}
	}
}
