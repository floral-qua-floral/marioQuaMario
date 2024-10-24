package com.floralquafloral.mariodata;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.floralquafloral.util.CPMIntegration;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector2d;

public class MarioPlayerData implements MarioData {
	private boolean enabled;
	private ParsedAction action;
	private ParsedPowerUp powerUp;
	private ParsedCharacter character;

	private PlayerEntity mario;
	@Override public PlayerEntity getMario() {
		return this.mario;
	}
	public void setMario(PlayerEntity mario) { this.mario = mario; }

	public MarioPlayerData(PlayerEntity mario) {
		this.mario = mario;
		this.enabled = true;
		this.action = RegistryManager.ACTIONS.get(Identifier.of("qua_mario:basic"));
		this.powerUp = RegistryManager.POWER_UPS.get(Identifier.of("qua_mario:super"));
		this.character = RegistryManager.CHARACTERS.get(Identifier.of("qua_mario:mario"));

		MarioQuaMario.LOGGER.info("Initialized a MarioData: {}, for {}", this, mario);
	}
	public MarioPlayerData(PlayerEntity mario, MarioData oldData) {
		this.mario = mario;
		this.enabled = oldData.isEnabled();
		this.action = oldData.getAction();
		this.powerUp = oldData.getPowerUp();
		this.character = oldData.getCharacter();

		MarioQuaMario.LOGGER.info("Initialized a MarioData from old data: {}, for {}, from {}", this, mario, oldData);
	}

	@Override public boolean useMarioPhysics() {
		return(
				isEnabled()
				&& !mario.getAbilities().flying
				&& !mario.isFallFlying()
				&& !mario.hasVehicle()
				&& !mario.isClimbing()
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
			double yawRad = Math.toRadians(mario.getYaw());
			this.negativeSineYaw = -Math.sin(yawRad);
			this.cosineYaw = Math.cos(yawRad);

			// Calculate current forwards and sideways velocity
			Vec3d currentVel = mario.getVelocity();
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
		if(this.VELOCITIES.isGenerated) return this.VELOCITIES.vertical;
		else return this.mario.getVelocity().y;
	}
	@Override public void setForwardVel(double forward) {
		VELOCITIES.ensureDirty().forward = forward;
	}
	@Override public void setStrafeVel(double strafe) {
		this.VELOCITIES.ensureDirty().strafe = strafe;
	}
	@Override public void setYVel(double vertical) {
		if(this.VELOCITIES.isGenerated) this.VELOCITIES.ensureDirty().vertical = vertical;
		else {
			Vec3d oldVel = this.mario.getVelocity();
			this.mario.setVelocity(oldVel.x, vertical, oldVel.z);
		}
	}
	@Override public void applyModifiedVelocity() {
		this.VELOCITIES.apply();
		this.VELOCITIES.isDirty = false;
		this.VELOCITIES.isGenerated = false;
	}

//	private double prevY;
	public void tick() {
		if(this.getMario().getWorld().isClient) {
			this.action.otherClientsTick(this);
			this.powerUp.otherClientsTick(this);
			this.character.otherClientsTick(this);
		}
		else {
			this.action.serverTick(this);
			this.powerUp.serverTick(this);
			this.character.serverTick(this);

//			if(this.action.STOMP != null) this.action.STOMP.attempt(this, prevY);
//			prevY = getMario().getY();

			this.applyModifiedVelocity();
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

	private class SkidSoundInstance extends MovingSoundInstance {
		private final PlayerEntity MARIO;
		private final boolean IS_WALL;
		private final SkidMaterial MATERIAL;
		private int ticks;

		protected SkidSoundInstance(boolean isWall, SkidMaterial material) {
			super(isWall ? MarioSFX.SKID_WALL : MarioSFX.SKID_BLOCK, SoundCategory.PLAYERS, SoundInstance.createRandom());
			this.MARIO = getMario();
			this.IS_WALL = isWall;
			this.MATERIAL = material;
			this.updatePos();
			this.repeat = true;
			this.repeatDelay = 0;
			this.volume = 1.0F;
			this.ticks = 0;
		}

		private static SkidMaterial getFloorSkidMaterial(PlayerEntity mario) {

			return SkidMaterial.BASIC;
		}

		private enum SkidMaterial {
			BASIC(MarioSFX.SKID_BLOCK);

			public final SoundEvent EVENT;
			SkidMaterial(SoundEvent event) {
				this.EVENT = event;
			}
		}

		@Override public void tick() {
			this.ticks++;
			MarioQuaMario.LOGGER.info("Ticking sound!");
			if(this.IS_WALL || this.MARIO.isOnGround()) {
				SkidMaterial newMaterial = getFloorSkidMaterial(this.MARIO);
				if(newMaterial != this.MATERIAL && !this.IS_WALL) {
					MarioQuaMario.LOGGER.info("Switching skid sound effect!");
					this.kill();
//					SkidSoundInstance.create(this.DATA, false);
				}
				else this.updatePos();
			}
			else this.volume = 0.0F;
		}

		private void updatePos() {
			this.x = this.MARIO.getX();
			this.y = this.MARIO.getY();
			this.z = this.MARIO.getZ();
			MarioQuaMario.LOGGER.info("X: " + this.x);
			if(this.IS_WALL) return;
			float slidingSpeed = (float) this.MARIO.getVelocity().horizontalLengthSquared();
			this.volume = Math.min(1.0F, ((float) ticks) / 3.0F) * Math.min(1.0F, 0.7F * slidingSpeed);
			this.pitch = 1.0F + Math.min(0.15F, 0.5F * slidingSpeed);
		}

		@Override public boolean shouldAlwaysPlay() {
			return true;
		}

		private void kill() {
			skidSFX = null;
			this.setDone();
		}
	}
	private SkidSoundInstance skidSFX;

	@Override public void setAction(ParsedAction action, long seed) {
		getAction().transitionTo(this, action, seed);
		this.setActionTransitionless(action);
	}
	@Override public void setActionTransitionless(ParsedAction action) {
		if(this.mario.getWorld().isClient) {
			// Skid SFX
			if(this.skidSFX != null) this.skidSFX.kill();
			if(action.SLIDING_STATUS.doSlideSfx() || action.SLIDING_STATUS.doWallSlideSfx()) {
				this.skidSFX = new SkidSoundInstance(action.SLIDING_STATUS.doWallSlideSfx(), SkidSoundInstance.getFloorSkidMaterial(getMario()));
				MinecraftClient.getInstance().getSoundManager().play(this.skidSFX);
				MarioQuaMario.LOGGER.info("SFX? " + this.skidSFX);
			}
		}
		else {
			if(this.action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.mario, this.action.ANIMATION, 0);
			if(action.ANIMATION != null)
				CPMIntegration.commonAPI.playAnimation(PlayerEntity.class, this.mario, action.ANIMATION, 1);
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
		this.mario.setHealth(this.mario.getMaxHealth());
		this.powerUp = powerUp;
		this.mario.calculateDimensions();
	}
	@Override public ParsedCharacter getCharacter() {
		return character;
	}
	@Override public void setCharacter(ParsedCharacter character) {
		this.character = character;
		this.mario.calculateDimensions();
	}
}
