package com.floralquafloral.mariodata;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.action.ParsedAction;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarioPlayerData implements MarioData {
	private boolean enabled;
	private ParsedAction action;
	private String powerUp;
	private String character;

	public final PlayerEntity MARIO;

	public MarioPlayerData(PlayerEntity mario) {
		this.MARIO = mario;
		this.setEnabled(true);
		this.action = RegistryManager.ACTIONS.get(Identifier.of("qua_mario:basic"));
		this.powerUp = "nil";
		this.character = "nil";

		MarioQuaMario.LOGGER.info("Initialized a MarioData: {}", this);
	}

	@Override public boolean useMarioPhysics() {
		return(
				isEnabled()
				&& !MARIO.getAbilities().flying
				&& !MARIO.isFallFlying()
				&& !MARIO.hasVehicle()
				&& !MARIO.isClimbing()
		);
	}

	@Override public void setVelocities(double forward, double strafe, @Nullable Double vertical) {
		double yawRad = Math.toRadians(MARIO.getYaw());
		double negativeSineYaw = -Math.sin(yawRad);
		double cosineYaw = Math.cos(yawRad);

		forward = MathHelper.clamp(forward, -3.75, 2.1);
		strafe = MathHelper.clamp(strafe, -1.9, 1.9);
		if(vertical == null) vertical = MARIO.getVelocity().y;


		MARIO.setVelocity(forward * negativeSineYaw + strafe * cosineYaw,
				vertical, forward * cosineYaw + strafe * -negativeSineYaw);
	}
	@Override public void setVelocities(MarioVelocityContainer velocities) {
		this.setVelocities(velocities.getForward(), velocities.getStrafe(), velocities.getVertical());
	}

	private MarioVelocityContainer velocities;
	@Override @NotNull public MarioVelocityContainer getVelocities() {
		if(this.velocities == null) this.velocities = new ReadOnlyVelocities();
		return this.velocities;
	}
	@Override public void clearCachedVelocities() {
		this.velocities = null;
	}

	@Override public void tick() {

	}

	@Override public boolean isEnabled() {
		return enabled;
	}
	@Override public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	@Override public ParsedAction getAction() {
		return action;
	}
	@Override public void setAction(ParsedAction action) {
		getAction().transitionTo(this, action);
		if(this.MARIO.getWorld().isClient) {
			if(this.action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.MARIO, this.action.ANIMATION, 0);
			if(action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.MARIO, action.ANIMATION, 1);
		}
		this.action = action;
	}
	@Override public void setActionTransitionless(ParsedAction action) {
		if(this.MARIO.getWorld().isClient) {
			if(this.action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.MARIO, this.action.ANIMATION, 0);
			if(action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.MARIO, action.ANIMATION, 1);
		}
		this.action = action;
	}
	@Override public String getPowerUp() {
		return powerUp;
	}
	@Override public void setPowerUp(String powerUp) {
		this.powerUp = powerUp;
	}
	@Override public String getCharacter() {
		return character;
	}
	@Override public void setCharacter(String character) {
		this.character = character;
	}

	private class ReadOnlyVelocities implements MarioVelocityContainer {
		private final double FORWARD, STRAFE, VERTICAL;

		public ReadOnlyVelocities() {
			// Calculate forward and sideways vector components
			double yawRad = Math.toRadians(MARIO.getYaw());
			double negativeSineYaw = -Math.sin(yawRad);
			double cosineYaw = Math.cos(yawRad);

			// Calculate current forwards and sideways velocity
			Vec3d currentVel = MARIO.getVelocity();
			this.FORWARD = currentVel.x * negativeSineYaw + currentVel.z * cosineYaw;
			this.STRAFE = currentVel.x * cosineYaw + currentVel.z * -negativeSineYaw;
			this.VERTICAL = currentVel.y;
		}

		@Override public double getForward() {
			return this.FORWARD;
		}
		@Override public double getStrafe() {
			return this.STRAFE;
		}
		@Override public double getVertical() {
			return this.VERTICAL;
		}
	}
}
