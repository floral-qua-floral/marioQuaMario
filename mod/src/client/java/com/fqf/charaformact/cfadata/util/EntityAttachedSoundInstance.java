package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class EntityAttachedSoundInstance extends MovingSoundInstance {
	protected final Entity ENTITY;

	public EntityAttachedSoundInstance(SoundEvent soundEvent, Entity entity, SoundCategory category, float pitch, float volume) {
		super(soundEvent, category, entity.getRandom());

		this.ENTITY = entity;
		this.updatePos();
		this.pitch = pitch;
		this.volume = volume;
	}

	@Override
	public void tick() {
		this.updatePos();
	}

	private void updatePos() {
		Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
		if((this.ENTITY == cameraEntity || cameraEntity == null) && !MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson()) {
			// This prevents an unpleasant effect that occurs when constantly setting the sound's position close to the camera.
			this.relative = true;
			this.x = 0; this.y = 0; this.z = 0;
		}
		else {
			this.relative = false;
			this.x = this.ENTITY.getX();
			this.y = this.ENTITY.getY();
			this.z = this.ENTITY.getZ();
		}
	}

	@Override
	public boolean isRelative() {
		return super.isRelative();
	}
}
