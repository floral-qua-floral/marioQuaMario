package com.floralquafloral.mariodata;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.action.ParsedAction;
import com.floralquafloral.registries.character.ParsedCharacter;
import com.floralquafloral.registries.powerup.ParsedPowerUp;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class MarioPlayerData implements MarioData {
	private boolean enabled;
	private ParsedAction action;
	private ParsedPowerUp powerUp;
	private ParsedCharacter character;

	private final PlayerEntity MARIO;
	@Override public PlayerEntity getMario() {
		return this.MARIO;
	}

	public MarioPlayerData(PlayerEntity mario) {
		this.MARIO = mario;
		this.setEnabled(true);
		this.action = RegistryManager.ACTIONS.get(Identifier.of("qua_mario:basic"));
		this.powerUp = RegistryManager.POWER_UPS.get(Identifier.of("qua_mario:super"));
		this.character = RegistryManager.CHARACTERS.get(Identifier.of("qua_mario:mario"));

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

	private class MarioVelocities {
		private double forward;
		private double strafe;
		private double vertical;
		private double negativeSineYaw;
		private double cosineYaw;
		private boolean isGenerated;
		private boolean isDirty;

		private MarioVelocities ensure() {
			if(this.isGenerated) return this;
			this.isGenerated = true;

			// Calculate forward and sideways vector components
			double yawRad = Math.toRadians(MARIO.getYaw());
			this.negativeSineYaw = -Math.sin(yawRad);
			this.cosineYaw = Math.cos(yawRad);

			// Calculate current forwards and sideways velocity
			Vec3d currentVel = MARIO.getVelocity();
			this.forward = currentVel.x * negativeSineYaw + currentVel.z * cosineYaw;
			this.strafe = currentVel.x * cosineYaw + currentVel.z * -negativeSineYaw;
			this.vertical = currentVel.y;

			return this;
		}
		private MarioVelocities ensureDirty() {
			this.isDirty = true;
			return this.ensure();
		}
		private void apply() {
			if(!this.isGenerated || !this.isDirty) return;
			getMario().setVelocity(this.forward * this.negativeSineYaw + this.strafe * this.cosineYaw,
					this.vertical, this.forward * this.cosineYaw + this.strafe * -this.negativeSineYaw);
		}
	}

	private final MarioVelocities VELOCITIES = new MarioVelocities();
	@Override public double getForwardVel() {
		return this.VELOCITIES.ensure().forward;
	}
	@Override public double getStrafeVel() {
		return this.VELOCITIES.ensure().strafe;
	}
	@Override public double getYVel() {
		return this.VELOCITIES.ensure().vertical;
	}
	@Override public void setForwardVel(double forward) {
		VELOCITIES.ensureDirty().forward = forward;
	}
	@Override public void setStrafeVel(double strafe) {
		this.VELOCITIES.ensureDirty().strafe = strafe;
	}
	@Override public void setYVel(double vertical) {
		this.VELOCITIES.ensureDirty().vertical = vertical;
	}
	@Override public void applyModifiedVelocity() {
		this.VELOCITIES.apply();
		this.VELOCITIES.isDirty = false;
		this.VELOCITIES.isGenerated = false;
	}

	public void tick() {
		if(this.getMario().getWorld().isClient) {
			this.action.otherClientsTick(this);
			this.powerUp.otherClientsTick(this);
//			this.character.otherClientsTick(this);
		}
		else {
			this.action.serverTick(this);
			this.powerUp.serverTick(this);
//			this.character.serverTick(this);
		}
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
	@Override public boolean getSneakProhibited() {
		return useMarioPhysics() && getAction().SNEAK_LEGALITY.prohibitSneak();
	}
	@Override public void setAction(ParsedAction action, long seed) {
		getAction().transitionTo(this, action, seed);
		this.setActionTransitionless(action);
	}
	@Override public void setActionTransitionless(ParsedAction action) {
		if(!this.MARIO.getWorld().isClient) {
			if(this.action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.MARIO, this.action.ANIMATION, 0);
			if(action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.MARIO, action.ANIMATION, 1);
		}
		this.action = action;
	}
	@Override public ParsedPowerUp getPowerUp() {
		return powerUp;
	}
	@Override public void setPowerUp(ParsedPowerUp powerUp) {
		MarioQuaMario.LOGGER.info("Set Power-up to {}", powerUp.ID);
		this.powerUp.losePower(this);
		powerUp.acquirePower(this);
		this.MARIO.setHealth(this.MARIO.getMaxHealth());
		this.powerUp = powerUp;
	}
	@Override public ParsedCharacter getCharacter() {
		return character;
	}
	@Override public void setCharacter(ParsedCharacter character) {
		this.character = character;
	}
}
