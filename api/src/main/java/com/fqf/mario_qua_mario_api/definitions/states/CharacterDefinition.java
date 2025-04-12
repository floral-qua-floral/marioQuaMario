package com.fqf.mario_qua_mario_api.definitions.states;

import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
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

	default float modifyIncomingDamage(IMarioAuthoritativeData data, DamageSource source, float amount) {
		return amount;
	}
}
