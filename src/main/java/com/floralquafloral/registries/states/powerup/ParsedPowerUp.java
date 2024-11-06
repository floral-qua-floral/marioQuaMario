package com.floralquafloral.registries.states.powerup;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.registries.states.ParsedMajorMarioState;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParsedPowerUp extends ParsedMajorMarioState {

	public final int VALUE;
	public final float VOICE_PITCH;
	public final SoundEvent ACQUISITION_SOUND;
	public final Identifier REVERT_TARGET;

	@NotNull public final PowerUpDefinition.PowerHeart HEART;
	@NotNull public final PowerUpDefinition.PowerHeart HEART_HARDCORE;
	@Nullable public final PowerUpDefinition.PowerHeart HEART_EMPTY;


	public ParsedPowerUp(PowerUpDefinition definition) {
		super(definition);

		this.VALUE = definition.getValue();
		this.VOICE_PITCH = definition.getVoicePitch();
		SoundEvent acquisitionSound = definition.getAcquisitionSound();
		this.ACQUISITION_SOUND = acquisitionSound == null ? MarioSFX.NORMAL_POWER : acquisitionSound;
		this.REVERT_TARGET = definition.getRevertTarget();

		this.HEART = definition.getHeart();
		PowerUpDefinition.PowerHeart hardcoreHeart = definition.getHeartHardcore();
		this.HEART_HARDCORE = hardcoreHeart == null ? this.HEART : hardcoreHeart;
		this.HEART_EMPTY = definition.getHeartEmpty();
	}

	public void acquirePower(MarioData data) {
		((PowerUpDefinition) this.DEFINITION).acquirePower(data);
	}
	public void losePower(MarioData data) {
		((PowerUpDefinition) this.DEFINITION).losePower(data);
	}
}
