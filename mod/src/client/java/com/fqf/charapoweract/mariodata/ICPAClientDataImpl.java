package com.fqf.charapoweract.mariodata;

import com.fqf.charapoweract_api.definitions.states.actions.util.animation.HandPreference;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.charapoweract.mariodata.util.*;
import com.fqf.charapoweract.registries.RegistryManager;
import com.fqf.charapoweract.registries.actions.AbstractParsedAction;
import com.fqf.charapoweract.registries.power_granting.ParsedPowerUp;
import com.fqf.charapoweract.util.MarioSFX;
import com.fqf.charapoweract_api.cpadata.ICPAAnimatingData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;

public interface ICPAClientDataImpl extends ICPAAnimatingData {
	@Override
	default boolean isClient() {
		return true;
	}

	@Override
	AbstractClientPlayerEntity getPlayer();

	@Override
	default void playAnimation(PlayermodelAnimation animation, int ticks) {
		this.getPlayer().mqm$getAnimationData().replaceAnimation((MarioPlayerData) this, animation, ticks);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(
			SoundEvent event, SoundCategory category,
			double x, double y, double z,
			float pitch, float volume, long seed
	) {
		SoundInstance sound = new PositionedSoundInstance(
				event, category,
				volume, pitch,
				Random.create(seed),
				x, y, z
		);
		MinecraftClient.getInstance().getSoundManager().play(sound);
		return new SoundInstanceWrapperImpl(sound);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, long seed) {
		return this.playSound(event, 1F, 1F, seed);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, float pitch, float volume, long seed) {
		Vec3d marioPos = this.getPlayer().getPos();
		return this.playSound(event, SoundCategory.PLAYERS, marioPos.x, marioPos.y, marioPos.z, pitch, volume, seed);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, Entity entity, SoundCategory category, long seed) {
		return this.playSound(event, category, entity.getX(), entity.getY(), entity.getZ(), 1F, 1F, seed);
	}

	Map<ICPAClientDataImpl, SoundInstance> MARIO_VOICE_LINES = new HashMap<>();

	default void handlePowerTransitionSound(boolean isReversion, ParsedPowerUp newPower, long seed) {
		if(isReversion) this.playSound(MarioSFX.REVERT, seed);
		else this.playSound(newPower.ACQUISITION_SOUND, seed);
	}

	// The stored sounds feature necessitates a tiny bit of duplicated code which is ANNOYING
	// To minimize the amount of such duplicated code, we leverage it for as much as possible
	Map<Identifier, SoundInstance> getStoredSounds();
	Identifier JUMP_IDENTIFIER = Identifier.of("mqm_fake_ids", "jump");
	Identifier COMMON_VOICE_IDENTIFIER = Identifier.of("mqm_fake_ids", "voices");
	Identifier SLIDE_IDENTIFIER = Identifier.of("mqm_fake_ids", "slide");

	default void handleSlidingSound(AbstractParsedAction newAction) {
		AbstractSlidingSoundInstance slidingSound = switch(newAction.SLIDING_STATUS) {
			case NOT_SLIDING, NOT_SLIDING_SMOOTH, SLIDING_SILENT -> null;
			case SLIDING -> new SlidingSoundInstance(this.getFloorSlidingSound(), this);
			case SKIDDING -> new SkiddingSoundInstance(this.getFloorSlidingSound(), this);
			case WALL_SLIDING -> new WallSlidingSoundInstance(this);
		};
		if(slidingSound != null)
			MinecraftClient.getInstance().getSoundManager().play(slidingSound);
		this.getStoredSounds().put(SLIDE_IDENTIFIER, slidingSound);
	}

	default SoundEvent getFloorSlidingSound() {
		return MarioSFX.SKID;
	}

	@Override
	default void playJumpSound(long seed) {
		this.fadeJumpSound();
		FadeableSoundInstance jumpSound = new FadeableSoundInstance((MarioPlayerData) this);
		MinecraftClient.getInstance().getSoundManager().play(jumpSound);
		this.getStoredSounds().put(JUMP_IDENTIFIER, jumpSound);
	}

	@Override
	default void fadeJumpSound() {
		FadeableSoundInstance jumpSoundInstance = (FadeableSoundInstance) this.getStoredSounds().get(JUMP_IDENTIFIER);
		if(jumpSoundInstance != null) jumpSoundInstance.fade();
	}

	@Override
	default SoundInstanceWrapperImpl voice(String voiceline, long seed) {
		MinecraftClient.getInstance().getSoundManager().stop(this.getStoredSounds().get(COMMON_VOICE_IDENTIFIER));
		Vec3d marioPos = this.getPlayer().getPos();
		if(RegistryManager.VOICE_LINES.get(voiceline) == null)
			throw new AssertionError("Voiceline " + voiceline + " isn't registered!!!");
		SoundInstanceWrapperImpl newVoiceSound = this.playSound(
				RegistryManager.VOICE_LINES.get(voiceline).get(((MarioPlayerData) this).getCharacter()), SoundCategory.VOICE,
				marioPos.x, marioPos.y, marioPos.z,
				this.getVoicePitch(), 1.0F,
				seed
		);

		this.getStoredSounds().put(COMMON_VOICE_IDENTIFIER, newVoiceSound.SOUND);

		return newVoiceSound;
	}

	@Override
	default float getVoicePitch() {
		return ((MarioPlayerData) this).getPowerUp().VOICE_PITCH;
	}

	@Override
	default void storeSound(SoundInstanceWrapper instance) {
		SoundInstance sound = ((SoundInstanceWrapperImpl) instance).SOUND;
		this.getStoredSounds().put(sound.getId(), sound);
	}

	@Override
	default void stopStoredSound(SoundEvent event) {
		MinecraftClient.getInstance().getSoundManager().stop(this.getStoredSounds().get(event.getId()));
	}

	@Override
	default void instantVisualRotate(float rotationDegrees, boolean counterRotateAnimation) {
		if(!this.getPlayer().isMainPlayer())
			this.getPlayer().setYaw(this.getPlayer().getYaw() + rotationDegrees);
//		this.getPlayer().bodyYaw += 90;
//		this.getPlayer().headYaw += 90;
//		if(counterRotateAnimation)
//			this.getPlayer().mqm$getAnimationData().everythingYawLerpDisplacement = rotationDegrees * MathHelper.RADIANS_PER_DEGREE * -1;
	}

	class SoundInstanceWrapperImpl implements SoundInstanceWrapper {
		private final SoundInstance SOUND;
		public SoundInstanceWrapperImpl(SoundInstance sound) {
			this.SOUND = sound;
		}
	}

	@Override
	default HandPreference getCurrentHandPreference() {
		return HandPreference.EITHER;
	}

	@Override
	default float getRelativeHeadYaw() {
		return 0;
	}
}
