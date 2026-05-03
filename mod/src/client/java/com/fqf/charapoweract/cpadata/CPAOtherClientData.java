package com.fqf.charapoweract.cpadata;

import com.fqf.charapoweract.registries.actions.AbstractParsedAction;
import com.fqf.charapoweract.registries.power_granting.ParsedPowerForm;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charapoweract_api.cpadata.util.RecordedCollisionSet;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.Map;

public class CPAOtherClientData extends CPAPlayerData implements ICPAClientDataImpl {
	public boolean jumpCapped;
	private final OtherClientPlayerEntity PLAYER;
	public CPAOtherClientData(OtherClientPlayerEntity player) {
		super();
		this.PLAYER = player;
	}
	@Override public OtherClientPlayerEntity getPlayer() {
		return this.PLAYER;
	}

	@Override
	public boolean setPowerUp(ParsedPowerForm newPowerUp, boolean isReversion, long seed) {
		this.handlePowerTransitionSound(isReversion, newPowerUp, seed);
		return super.setPowerUp(newPowerUp, isReversion, seed);
	}

	private boolean replaceAnimationNextTick;
	private PlayermodelAnimation nextTickAnimation;
	private boolean replaceAnimationNextNextTick;
	private PlayermodelAnimation nextNextTickAnimation;
	@Override public void setActionTransitionless(AbstractParsedAction action) {
		this.handleSlidingSound(action);
		this.replaceAnimationNextTick = true;
		this.nextTickAnimation = action.ANIMATION;
		super.setActionTransitionless(action);
	}

	private double prevX, prevY, prevZ, deltaX, deltaY, deltaZ;

	@Override public void tick() {
		super.tick();
		this.getAction().clientTick(this, false);
		this.getPowerForm().clientTick(this, false);
		this.getCharacter().clientTick(this, false);

		Vec3d pos = PLAYER.getPos();
		this.deltaX = pos.x - prevX;
		this.deltaY = pos.y - prevY;
		this.deltaZ = pos.z - prevZ;
		this.prevX = pos.x;
		this.prevY = pos.y;
		this.prevZ = pos.z;

		this.VELOCITIES.invalidate();

		if(this.replaceAnimationNextTick) {
			this.replaceAnimationNextTick = false;
			this.PLAYER.cpa$getAnimationData().replaceAnimation(this, this.nextTickAnimation, -1);
		}
		if(this.replaceAnimationNextNextTick) {
			this.replaceAnimationNextNextTick = false;
			this.replaceAnimationNextTick = true;
			this.nextTickAnimation = this.nextNextTickAnimation;
		}
	}

	private final InferredVelocities VELOCITIES = new InferredVelocities();

	@Override
	public void playCameraAnimation(CameraAnimationSet animationSet) {
		// Do nothing
	}

	private class InferredVelocities {
		private double forward;
		private double strafe;
		private double vertical;
		private boolean isGenerated;

		private InferredVelocities ensure() {
			if(this.isGenerated) return this;
			this.isGenerated = true;

			// Calculate forward and sideways vector components
			double yawRad = Math.toRadians(CPAOtherClientData.this.getPlayer().getYaw());
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
	public Vec3d getVelocity() {
		return new Vec3d(this.deltaX, this.deltaY, this.deltaZ);
	}

	@Override
	public RecordedCollisionSet getRecordedCollisions() {
		return CPAMoveableData.EMPTY_RECORDED_COLLISION_SET;
	}

	@Override
	public double getDeltaYaw() {
		return 0;
	}

	@Override
	public Inputs getInputs() {
		return CPAServerPlayerData.PHONY_INPUTS;
	}
}
