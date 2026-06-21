package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface CharacterDefinition extends StatAlteringStateDefinition {
	@NotNull String defineVoiceName();

	@NotNull Identifier defineInitialAction();
	@NotNull Identifier defineInitialForm();

	@NotNull SoundEvent defineJumpSound();
	@NotNull Identifier chooseMountedAction(CfaData data, Entity vehicle);

	default float modifyIncomingDamage(CfaAuthoritativeData data, DamageSource source, float amount) {
		return amount;
	}
}
