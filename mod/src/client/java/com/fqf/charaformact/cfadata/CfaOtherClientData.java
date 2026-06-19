package com.fqf.charaformact.cfadata;

import com.fqf.charaformact.appearance.ParsedCommonAppearance;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.HandPreference;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.util.RecordedCollisionSet;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.Map;

public class CfaOtherClientData extends CfaPlayerData implements CfaClientDataImpl {
	private final OtherClientPlayerEntity PLAYER;
	public final CfaAppearanceData<CfaOtherClientData> APPEARANCE_DATA;
	public CfaOtherClientData(OtherClientPlayerEntity player) {
		super();
		this.PLAYER = player;
		this.APPEARANCE_DATA = new CfaAppearanceData<>(this);
	}
	@Override public OtherClientPlayerEntity getPlayer() {
		return this.PLAYER;
	}

	@Override
	public boolean setForm(ParsedForm newForm, boolean isReversion, long seed) {
		ParsedForm oldForm = this.getForm();
		boolean formChanged = super.setForm(newForm, isReversion, seed);
		if(formChanged) {
			this.handlePowerTransitionSound(isReversion, oldForm, newForm, seed);
			this.APPEARANCE_DATA.conditionallyFlicker();
		}
		return formChanged;
	}

	@Override public void setActionTransitionless(AbstractParsedAction action) {
		this.handleSlidingSound(action);
		super.setActionTransitionless(action);
		this.APPEARANCE_DATA.updateAction();
	}

	@Override public void updateAppearance() {
		this.APPEARANCE_DATA.updateAppearance();
	}
	@Override public @Nullable ParsedCommonAppearance getAppearance() {
		return this.APPEARANCE_DATA.getAppearance();
	}

	private double prevX, prevY, prevZ, deltaX, deltaY, deltaZ;

	@Override public void tick() {
		super.tick();
		this.getAction().clientTick(this, false);
		this.getForm().clientTick(this, false);
		this.getCharacter().clientTick(this, false);

		Vec3d pos = PLAYER.getPos();
		this.deltaX = pos.x - prevX;
		this.deltaY = pos.y - prevY;
		this.deltaZ = pos.z - prevZ;
		this.prevX = pos.x;
		this.prevY = pos.y;
		this.prevZ = pos.z;

		this.VELOCITIES.invalidate();

		this.APPEARANCE_DATA.tick();
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
			double yawRad = Math.toRadians(CfaOtherClientData.this.getPlayer().getYaw());
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
		return CfaMoveableData.EMPTY_RECORDED_COLLISION_SET;
	}

	@Override
	public double getDeltaYaw() {
		return 0;
	}

	@Override
	public Inputs getInputs() {
		return CfaServerPlayerData.PHONY_INPUTS;
	}

	private HandPreference handPreference = HandPreference.EITHER;
	private float relativeHeadYaw;
	@Override public void setHandPreferenceAndRelativeHeadYaw(HandPreference preference, float relativeHeadYaw) {
		this.handPreference = preference;
		this.relativeHeadYaw = relativeHeadYaw;
	}
	@Override public HandPreference getCurrentHandPreference() {
		return this.handPreference;
	}
	@Override public float getRelativeHeadYawRadians() {
		return this.relativeHeadYaw;
	}
}
