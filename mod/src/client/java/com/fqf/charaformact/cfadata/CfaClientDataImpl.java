package com.fqf.charaformact.cfadata;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.HandPreference;
import com.fqf.charaformact.cfadata.util.*;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.util.CfaSounds;
import com.fqf.charaformact_api.util.BuiltInPowers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Map;

public interface CfaClientDataImpl extends CfaAnimatingData {
	float DEFAULT_VOLUME = 0.4F;

	@Override
	default boolean isClient() {
		return true;
	}

	@Override
	AbstractClientPlayerEntity getPlayer();

	@Override
	default void playAnimation(AnimationDefinition animation, int duration) {
		this.getPlayer().cfa$getAppearanceData().triggerAnimation(animation, duration);
//		this.getPlayer().cfa$getOldAnimationData().replaceAnimation((CfaPlayerData) this, animation, ticks);
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
		return this.playSound(event, 1F, DEFAULT_VOLUME, seed);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, float pitch, float volume, long seed) {
		Vec3d position = this.getPlayer().getPos();
		return this.playSound(event, SoundCategory.PLAYERS, position.x, position.y, position.z, pitch, volume, seed);
	}

	@Override
	default SoundInstanceWrapperImpl playSound(SoundEvent event, Entity entity, SoundCategory category, long seed) {
		return this.playSound(event, category, entity.getX(), entity.getY(), entity.getZ(), 1F, DEFAULT_VOLUME, seed);
	}

	@Override
	default void sustainSound(SoundEvent event, Entity entity, SoundCategory category) {
		this.sustainSound(event, entity, category, 1.0F, DEFAULT_VOLUME);
	}

	@Override
	default void sustainSound(SoundEvent event, Entity entity, SoundCategory category, float pitch, float volume) {
		SustainingSoundInstance existingInstance = this.getStoredSounds().get(event.getId()) instanceof SustainingSoundInstance sustaining
				? sustaining
				: null;

		SoundManager manager = MinecraftClient.getInstance().getSoundManager();
		if(existingInstance == null || !manager.isPlaying(existingInstance)) {
			SustainingSoundInstance newInstance = new SustainingSoundInstance(event, entity, category, pitch, volume);
			manager.play(newInstance);
			this.storeSound(new SoundInstanceWrapperImpl(newInstance));
		}
		else existingInstance.sustain(pitch, volume);
	}

	default void handlePowerTransitionSound(boolean isReversion, ParsedForm newPower, long seed) {
		if(isReversion) this.playSound(CfaSounds.REVERT, seed);
		else this.playSound(newPower.ACQUISITION_SOUND, seed);
	}

	// The stored sounds feature necessitates a tiny bit of duplicated code which is ANNOYING
	// To minimize the amount of such duplicated code, we leverage it for as much as possible
	Map<Identifier, SoundInstance> getStoredSounds();
	Identifier JUMP_IDENTIFIER = Identifier.of("charaformact_fake_ids", "jump");
	Identifier COMMON_VOICE_IDENTIFIER = Identifier.of("charaformact_fake_ids", "voices");
	Identifier SLIDE_IDENTIFIER = Identifier.of("charaformact_fake_ids", "slide");

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
		return CfaSounds.SKID;
	}

	@Override
	default void playJumpSound(long seed) {
		this.fadeJumpSound();
		FadeableSoundInstance jumpSound = new FadeableSoundInstance((CfaPlayerData) this);
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
		return this.voice(voiceline, DEFAULT_VOLUME, seed);
	}

	@Override
	default SoundInstanceWrapperImpl voice(String voiceline, float volume, long seed) {
		AbstractClientPlayerEntity player = this.getPlayer();

		// Checking if air is full instead of checking for fluid immersion to hopefully catch any weird modded fluids,
		// space dimensions, etc.
		if(player.getAir() < player.getMaxAir()) {
			if(CharaFormAct.CONFIG.doSuppressVoiceUnderwater() && !this.hasPower(BuiltInPowers.CAN_USE_VOICE_WITHOUT_BREATH))
				return new SoundInstanceWrapperImpl(this.getStoredSounds().get(COMMON_VOICE_IDENTIFIER));
		}

		MinecraftClient.getInstance().getSoundManager().stop(this.getStoredSounds().get(COMMON_VOICE_IDENTIFIER));

		if(RegistryManager.VOICE_LINES.get(voiceline) == null)
			throw new IllegalArgumentException("Voiceline " + voiceline + " isn't registered!!!");

		SoundInstance newVoiceSound = new EntityAttachedSoundInstance(
				RegistryManager.VOICE_LINES.get(voiceline).get(((CfaPlayerData) this).getCharacter()),
				player, SoundCategory.VOICE, this.getVoicePitch(), volume
		);

		MinecraftClient.getInstance().getSoundManager().play(newVoiceSound);
		this.getStoredSounds().put(COMMON_VOICE_IDENTIFIER, newVoiceSound);

		return new SoundInstanceWrapperImpl(newVoiceSound);
	}

	@Override
	default float getVoicePitch() {
		return ((CfaPlayerData) this).getForm().VOICE_PITCH;
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
		PlayerEntity player = this.getPlayer();
		float newYaw = player.getYaw() + rotationDegrees;
		float newBodyYaw = player.getBodyYaw() + rotationDegrees;

		player.setYaw(newYaw);
		player.headYaw = newYaw;
		player.prevHeadYaw = newYaw;
		player.bodyYaw = newBodyYaw;
		player.prevBodyYaw = newBodyYaw;
	}

	class SoundInstanceWrapperImpl implements SoundInstanceWrapper {
		private final SoundInstance SOUND;
		public SoundInstanceWrapperImpl(SoundInstance sound) {
			this.SOUND = sound;
		}
	}

	default void updateHandPreferenceAndRelativeHeadYaw(boolean rightArmBusy, boolean leftArmBusy, float headRelativeYaw) {
		if(rightArmBusy) {
			if(leftArmBusy) this.setHandPreferenceAndRelativeHeadYaw(HandPreference.NEITHER, headRelativeYaw);
			else this.setHandPreferenceAndRelativeHeadYaw(HandPreference.PREFER_LEFT, headRelativeYaw);
		}
		else {
			if(leftArmBusy) this.setHandPreferenceAndRelativeHeadYaw(HandPreference.PREFER_RIGHT, headRelativeYaw);
			else this.setHandPreferenceAndRelativeHeadYaw(HandPreference.EITHER, headRelativeYaw);
		}
	}
	void setHandPreferenceAndRelativeHeadYaw(HandPreference preference, float relativeHeadYaw);

	@Override
	default float getRelativeHeadYawDegrees() {
		return this.getRelativeHeadYawRadians() * MathHelper.DEGREES_PER_RADIAN;
	}
}
