package com.fqf.charaformact.registries.power_granting;

import com.fqf.charaformact.registries.ImmutableCollectionHelper;
import com.fqf.charaformact.registries.ParsedAttackInterceptingState;
import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact.registries.ParsedAttackInterception;
import com.fqf.charaformact.registries.actions.AnimationHelperImpl;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParsedForm extends ParsedPowerGrantingState implements ParsedAttackInterceptingState {
	public final Identifier REVERSION_TARGET_ID;
	public final int VALUE;

	public final boolean DO_FLICKER_ANIMATION;
	public final @Nullable SoundEvent REVERSION_SOUND;
	public final @Nullable SoundEvent ACQUISITION_SOUND;
	public final float VOICE_PITCH;
	public final float JUMP_PITCH;

	public final FormDefinition.FormHeart HEART;

	private final List<ParsedAttackInterception> INTERCEPTIONS;

	public ParsedForm(Identifier id, FormDefinition definition) {
		super(id, definition);

		this.DO_FLICKER_ANIMATION = definition.doFlickerAnimation();
		this.REVERSION_TARGET_ID = definition.defineReversionTarget();
		this.VALUE = definition.defineValue();

		this.REVERSION_SOUND = definition.defineReversionSound();
		this.ACQUISITION_SOUND = definition.defineAcquisitionSound();
		this.VOICE_PITCH = definition.defineVoicePitch();
		this.JUMP_PITCH = definition.defineJumpPitch();

		this.HEART = definition.defineFormHeart(new FormHeartHelperImpl(this.ID));

		List<AttackInterceptingStateDefinition.AttackInterceptionDefinition> interceptionDefinitions;
		interceptionDefinitions = ImmutableCollectionHelper.accumulateList(builder -> definition.accumulateAttackInterceptions(builder, AnimationHelperImpl.INSTANCE));
		this.INTERCEPTIONS = interceptionDefinitions.stream().map(interceptionDefinition ->
				new ParsedAttackInterception(interceptionDefinition, false)).toList();
	}

	@Override public List<ParsedAttackInterception> getInterceptions() {
		return INTERCEPTIONS;
	}
}
