package com.fqf.charaformact.registries.power_granting;

import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact.registries.ParsedAttackInterception;
import com.fqf.charaformact.registries.actions.AnimationHelperImpl;
import com.fqf.charaformact.util.CfaSounds;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ParsedForm extends ParsedPowerGrantingState {
	public final Identifier REVERSION_TARGET_ID;
	public final int VALUE;

	public final SoundEvent ACQUISITION_SOUND;
	public final float VOICE_PITCH;
	public final float JUMP_PITCH;

	public final FormDefinition.FormHeart HEART;

	public final List<ParsedAttackInterception> INTERCEPTIONS;

	public ParsedForm(FormDefinition definition) {
		super(definition);

		this.REVERSION_TARGET_ID = definition.getReversionTarget();
		this.VALUE = definition.getValue();

		SoundEvent definedAcquisitionSound = definition.getAcquisitionSound();
		this.ACQUISITION_SOUND = definedAcquisitionSound == null ? CfaSounds.EMPOWER : definedAcquisitionSound;
		this.VOICE_PITCH = definition.getVoicePitch();
		this.JUMP_PITCH = definition.getJumpPitch();

		this.HEART = definition.getFormHeart(new FormHeartHelperImpl(this.RESOURCE_ID));

		this.INTERCEPTIONS = new ArrayList<>();
		for (AttackInterceptingStateDefinition.AttackInterceptionDefinition interception : definition.getAttackInterceptions(AnimationHelperImpl.INSTANCE)) {
			this.INTERCEPTIONS.add(new ParsedAttackInterception(interception, false));
		}
	}
}
