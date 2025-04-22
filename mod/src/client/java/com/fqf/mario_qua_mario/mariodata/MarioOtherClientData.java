package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.Map;

public class MarioOtherClientData extends MarioPlayerData implements IMarioClientDataImpl {
	public boolean jumpCapped;
	private final OtherClientPlayerEntity MARIO;
	public MarioOtherClientData(OtherClientPlayerEntity mario) {
		super();
		this.MARIO = mario;
	}
	@Override public OtherClientPlayerEntity getMario() {
		return this.MARIO;
	}

	@Override
	public boolean setPowerUp(ParsedPowerUp newPowerUp, boolean isReversion, long seed) {
		this.handlePowerTransitionSound(isReversion, newPowerUp, seed);
		return super.setPowerUp(newPowerUp, isReversion, seed);
	}

	private boolean replaceAnimationNextTick;
	private PlayermodelAnimation nextTickAnimation;
	private boolean replaceAnimationNextNextTick;
	private PlayermodelAnimation nextNextTickAnimation;
	@Override public void setActionTransitionless(AbstractParsedAction action) {
		this.handleSlidingSound(action);
		this.replaceAnimationNextNextTick = true;
		this.nextNextTickAnimation = action.ANIMATION;
		super.setActionTransitionless(action);
	}

	private double prevX, prevY, prevZ, deltaX, deltaY, deltaZ;

	@Override public void tick() {
		super.tick();
		this.getAction().clientTick(this, false);
		this.getPowerUp().clientTick(this, false);
		this.getCharacter().clientTick(this, false);

		Vec3d pos = MARIO.getPos();
		this.deltaX = pos.x - prevX;
		this.deltaY = pos.y - prevY;
		this.deltaZ = pos.z - prevZ;
		this.prevX = pos.x;
		this.prevY = pos.y;
		this.prevZ = pos.z;

		this.VELOCITIES.invalidate();

		if(this.replaceAnimationNextTick) {
			this.replaceAnimationNextTick = false;
			this.MARIO.mqm$getAnimationData().replaceAnimation(this, this.nextTickAnimation, -1);
		}
		if(this.replaceAnimationNextNextTick) {
			this.replaceAnimationNextNextTick = false;
			this.replaceAnimationNextTick = true;
			this.nextTickAnimation = this.nextNextTickAnimation;
		}
	}

	private final MarioInferredVelocities VELOCITIES = new MarioInferredVelocities();
	private class MarioInferredVelocities {
		private double forward;
		private double strafe;
		private double vertical;
		private boolean isGenerated;

		private MarioInferredVelocities ensure() {
			if(this.isGenerated) return this;
			this.isGenerated = true;

			// Calculate forward and sideways vector components
			double yawRad = Math.toRadians(getMario().getYaw());
			double negativeSineYaw = -Math.sin(yawRad);
			double cosineYaw = Math.cos(yawRad);

			// Calculate current forwards and sideways velocity
			this.forward = deltaX * negativeSineYaw + deltaZ * cosineYaw;
			this.strafe = deltaX * cosineYaw + deltaZ * -negativeSineYaw;
			this.vertical = deltaY;

			return this;
		}

		private void invalidate() {
			this.isGenerated = false;
		}
	}

	private final Map<Identifier, SoundInstance> STORED_SOUNDS = new HashMap<>();
	@Override public Map<Identifier, SoundInstance> getStoredSounds() {
		return this.STORED_SOUNDS;
	}

	@Override
	public double getForwardVel() {
		return this.VELOCITIES.ensure().forward;
	}

	@Override
	public double getStrafeVel() {
		return this.VELOCITIES.ensure().strafe;
	}

	@Override
	public double getYVel() {
		return this.VELOCITIES.ensure().vertical;
	}

	@Override
	public double getHorizVel() {
		return Vector2d.length(this.deltaX, this.deltaZ);
	}

	@Override
	public double getHorizVelSquared() {
		return Vector2d.lengthSquared(this.deltaX, this.deltaZ);
	}

	@Override
	public double getDeltaYaw() {
		return 0;
	}

	@Override
	public MarioInputs getInputs() {
		return MarioServerPlayerData.PHONY_INPUTS;
	}
}
