package com.fqf.charaformact.registries.power_granting;

import com.fqf.charaformact.registries.ImmutableCollectionHelper;
import com.fqf.charaformact.registries.ParsedAttackInterceptingState;
import com.fqf.charaformact.registries.ParsedAttackInterception;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AnimationHelperImpl;
import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
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

	private int healthBarCount;
	private @Nullable Identifier reversionTargetID;
	private @Nullable ParsedForm reversionTarget;

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

		this.healthBarCount = 0;
		this.reversionTargetID = definition.defineReversionTarget();
		this.reversionTarget = null;
	}

	@Override public List<ParsedAttackInterception> getInterceptions() {
		return INTERCEPTIONS;
	}

	public @Nullable ParsedForm getReversionTarget() {
		if(this.reversionTargetID != null) {
			this.reversionTarget = RegistryManager.FORMS.get(this.reversionTargetID);
			if(this.reversionTarget == null) throw new IllegalArgumentException("Invalid reversion target for "
					+ this.ID + ": No form registered to ID " + this.reversionTargetID);
			this.reversionTargetID = null; // This is so we don't have to evaluate it again
		}
		return this.reversionTarget;
	}

	private static final int MAXIMUM_RECURSIONS = 100;
	private int countHealthBars(ParsedForm original, int recursions) {
		if(recursions > MAXIMUM_RECURSIONS)
			throw new IllegalArgumentException("Invalid reversion target for " + original.ID + ": Form has more than "
					+ MAXIMUM_RECURSIONS + " health bars?!?!");
		else if(this.getReversionTarget() == null)
			return 1;
		else
			return this.getReversionTarget().countHealthBars(original, recursions + 1) + 1;
	}

	public int getHealthBarCount() {
		if(this.healthBarCount == 0) this.healthBarCount = this.countHealthBars(this, 0);
		return this.healthBarCount;
	}
}
