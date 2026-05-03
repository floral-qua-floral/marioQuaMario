package com.fqf.charapoweract_api.definitions.states;

import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface CharacterDefinition extends StatAlteringStateDefinition {
	default @NotNull String getVoiceName() {
		return this.getID().getPath();
	}

	@NotNull Identifier getInitialAction();
	@NotNull Identifier getInitialPowerUp();

	@NotNull SoundEvent getJumpSound();
	@NotNull Identifier getMountedAction(Entity vehicle);

	float getEyeHeightFactor();

	default float modifyIncomingDamage(ICPAAuthoritativeData data, DamageSource source, float amount) {
		return amount;
	}
}
