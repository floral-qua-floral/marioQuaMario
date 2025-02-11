package com.fqf.mario_qua_mario.characters;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import com.fqf.mario_qua_mario.util.MarioVars;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class Mario implements CharacterDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("mario");
	}

	@Override public @NotNull Identifier getInitialAction() {
		return MarioQuaMarioContent.makeID("fall");
	}
	@Override public @NotNull Identifier getInitialPowerUp() {
		return MarioQuaMarioContent.makeID("super");
	}

	@Override public @NotNull Identifier getMountedAction(Entity vehicle) {
		return MarioQuaMarioContent.makeID("mounted");
	}
	@Override public @NotNull SoundEvent getJumpSound() {
		return MarioContentSFX.MARIO_JUMP;
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 0.97F;
	}
	@Override public float getAnimationWidthFactor() {
		return 1;
	}
	@Override public float getAnimationHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}

	@Override public Set<String> getPowers() {
		return Set.of(
				Powers.LIGHTNING_SHRINKS
		);
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new MarioVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		MarioVars.get(data).canDoubleJumpTicks--;
		MarioVars.get(data).canTripleJumpTicks--;
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {
		MarioVars.get(data).canDoubleJumpTicks--;
		MarioVars.get(data).canTripleJumpTicks--;
	}
}
