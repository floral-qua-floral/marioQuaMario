package com.floralquafloral.mariodata;

import com.floralquafloral.*;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.util.MarioSFX;
import com.floralquafloral.definitions.actions.StatCategory;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public abstract class MarioPlayerData implements MarioData {
	private static final Identifier FALL_RESISTANCE_ID = Identifier.of(MarioQuaMario.MOD_ID, "mario_fall_resistance");
	private static final EntityAttributeModifier FALL_RESISTANCE = new EntityAttributeModifier(
			FALL_RESISTANCE_ID, 8, EntityAttributeModifier.Operation.ADD_VALUE
	);

	private static final Identifier ATTACK_SLOWDOWN_ID = Identifier.of(MarioQuaMario.MOD_ID, "mario_fall_resistance");
	private static final EntityAttributeModifier ATTACK_SLOWDOWN = new EntityAttributeModifier(
			ATTACK_SLOWDOWN_ID, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);



	private boolean enabled;
	private final boolean IS_CLIENT;
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
		this.IS_CLIENT = mario.getWorld().isClient;
		this.enabled = false;
		this.action = RegistryManager.ACTIONS.get(Identifier.of("qua_mario:basic"));
		this.powerUp = RegistryManager.POWER_UPS.get(Identifier.of("qua_mario:super"));
		this.character = RegistryManager.CHARACTERS.get(Identifier.of("qua_mario:mario"));

//		this.setEnabled(true);
//		this.setActionTransitionless(this.action);
//		this.setPowerUp(this.powerUp);
//		this.setCharacter(this.character);

		MarioQuaMario.LOGGER.info("Initialized a MarioData: {}, for {}", this, mario);
	}

	@Override public boolean isClient() {
		return this.IS_CLIENT;
	}

	@Override public boolean useMarioPhysics() {
		return(
				isEnabled()
				&& !this.mario.getAbilities().flying
				&& !this.mario.isFallFlying()
//				&& !mario.hasVehicle()
//				&& !mario.isClimbing()
		);
	}

	private static final double MOVEMENT_SPEED_MULTIPLIER = 1.0 / 0.10000000149011612;
	private static final double MOVEMENT_SPEED_MULTIPLIER_SPRINTING = 1.0 / 0.13000000312924387;

	@Override
	public double getStat(CharaStat stat) {
		double modifiedBase = stat.BASE * this.getStatMultiplier(stat);

		double attributeFactor = 1.0;
		double attributeAddend = 0.0;

		if(stat.CATEGORIES.contains(StatCategory.SPEED) && (
				stat.CATEGORIES.contains(StatCategory.WALKING)
						|| stat.CATEGORIES.contains(StatCategory.RUNNING)
						|| stat.CATEGORIES.contains(StatCategory.P_RUNNING)
						|| stat.CATEGORIES.contains(StatCategory.DUCKING)
		)) {
			attributeFactor = this.mario.getAttributes().getValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
					* (this.mario.isSprinting() ? MOVEMENT_SPEED_MULTIPLIER_SPRINTING : MOVEMENT_SPEED_MULTIPLIER);
		}

		if(stat.CATEGORIES.contains(StatCategory.JUMP_VELOCITY)) {
			attributeAddend = this.mario.getJumpBoostVelocityModifier();
		}

		return attributeFactor * modifiedBase + attributeAddend;
	}

	@Override
	public double getStatMultiplier(CharaStat stat) {
		return this.powerUp.getStatMultiplier(stat) * this.character.getStatMultiplier(stat);
	}

	@Override
	public int getBumpStrengthModifier() {
		return this.powerUp.BUMP_STRENGTH_MODIFIER + this.character.BUMP_STRENGTH_MODIFIER;
	}

	public float getVoicePitch() {
		return this.powerUp.VOICE_PITCH;
	}

	public boolean attemptDismount = false;

	public abstract void tick();

	@Override public boolean isEnabled() {
		return enabled;
	}
	public void setEnabledInternal(boolean enabled) {
		this.enabled = enabled;
		EntityAttributeInstance safeFallAttributeInstance = this.mario.getAttributeInstance(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
		EntityAttributeInstance attackSpeedAttributeInstance = this.mario.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		assert safeFallAttributeInstance != null && attackSpeedAttributeInstance != null;
		safeFallAttributeInstance.removeModifier(FALL_RESISTANCE_ID);
		safeFallAttributeInstance.addPersistentModifier(FALL_RESISTANCE);
		attackSpeedAttributeInstance.removeModifier(ATTACK_SLOWDOWN_ID);
		attackSpeedAttributeInstance.addPersistentModifier(ATTACK_SLOWDOWN);
	}
	public ParsedAction getAction() {
		return action;
	}
	@Override public Identifier getActionID() {
		return action.ID;
	}
	@Override public boolean isSneakProhibited() {
		return useMarioPhysics() && getAction().SNEAK_LEGALITY.prohibitSneak();
	}

	private class SkidSoundInstance extends MovingSoundInstance {
		private final boolean IS_WALL;
		private final boolean SPEED_SCALING;
		private final SkidMaterial MATERIAL;
		private int ticks;

		protected SkidSoundInstance(boolean isWall, SkidMaterial material, boolean scaling) {
			super(isWall ? MarioSFX.SKID_WALL : material.EVENT, SoundCategory.PLAYERS, SoundInstance.createRandom());
			this.IS_WALL = isWall;
			this.SPEED_SCALING = scaling;
			this.MATERIAL = material;
			this.updatePos();
			this.repeat = true;
			this.repeatDelay = 0;
			this.volume = 1.0F;
			this.ticks = 0;
		}

		private static SkidMaterial getFloorSkidMaterial(PlayerEntity mario) {
			BlockState block = mario.getWorld().getBlockState(mario.getVelocityAffectingPos());
			if(block.getBlock().getSlipperiness() > 0.85) return SkidMaterial.ICE;
			return SkidMaterial.BASIC;
		}

		private enum SkidMaterial {
			ICE(MarioSFX.SKID_ICE),
			BASIC(MarioSFX.SKID_BLOCK);

			public final SoundEvent EVENT;
			SkidMaterial(SoundEvent event) {
				this.EVENT = event;
			}
		}

		@Override public void tick() {
			if(isDone()) return; //WHY???
			this.ticks++;
			if(this.IS_WALL || getMario().isOnGround()) {
				SkidMaterial newMaterial = getFloorSkidMaterial(getMario());
				if(newMaterial != this.MATERIAL && !this.IS_WALL) {
					MarioQuaMario.LOGGER.info("Switching skid material from {} to {}!", this.MATERIAL, newMaterial);
					makeSkidSFX(false, newMaterial, this.SPEED_SCALING);

				}
				else this.updatePos();
			}
			else this.volume = 0.0F;
		}

		private void updatePos() {
			this.x = getMario().getX();
			this.y = getMario().getY();
			this.z = getMario().getZ();
			if(this.IS_WALL) return;
			if(this.SPEED_SCALING) {
				float slidingSpeed = (float) getMario().getVelocity().horizontalLengthSquared();
				this.volume = Math.min(1.0F, ((float) ticks) / 3.0F) * Math.min(1.0F, 0.7F * slidingSpeed);
				this.pitch = 1.0F + Math.min(0.15F, 0.5F * slidingSpeed);
			}
			else {
				float slidingSpeed = (float) getMario().getVelocity().horizontalLengthSquared();
				this.volume = Math.min(1.0F, ((float) ticks) / 3.0F) * Math.min(1.0F, 0.4F + 0.7F * slidingSpeed);
				this.pitch = 1.0F + Math.min(0.15F, 0.5F * slidingSpeed);
			}
		}

		@Override public boolean shouldAlwaysPlay() {
			return true;
		}

		private void kill() {
			this.setDone();
//			skidSFX = null;
		}
	}
	private SkidSoundInstance skidSFX;
	private void makeSkidSFX(boolean isWall, SkidSoundInstance.SkidMaterial material, boolean scaling) {
		if(this.skidSFX != null) this.skidSFX.kill();
		this.skidSFX = new SkidSoundInstance(isWall, material, scaling);
		MinecraftClient.getInstance().getSoundManager().playNextTick(this.skidSFX);
	}
	private void makeSkidSFX(boolean isWall, boolean scaling) {
		this.makeSkidSFX(isWall, SkidSoundInstance.getFloorSkidMaterial(this.getMario()), scaling);
	}

	public void setAction(ParsedAction action, long seed) {
		getAction().transitionTo(this, action, seed);
		this.setActionTransitionless(action);
	}
	public void setActionTransitionless(ParsedAction action) {
		if(this.isClient()) {
			// Skid SFX
			if(this.skidSFX != null) this.skidSFX.kill();
			if(action.SLIDING_STATUS.doSlideSfx() || action.SLIDING_STATUS.doWallSlideSfx()) {
				makeSkidSFX(action.SLIDING_STATUS.doWallSlideSfx(), action.SLIDING_STATUS.doSpeedScaling());
			}
		}

		this.action = action;
		this.mario.setPose(this.mario.getPose());

	}
	public ParsedPowerUp getPowerUp() {
		return powerUp;
	}
	@Override public Identifier getPowerUpID() {
		return powerUp.ID;
	}
	public void setPowerUp(ParsedPowerUp powerUp) {
		MarioQuaMario.LOGGER.info("Set Power-up to {}", powerUp.ID);
		this.powerUp.losePower(this);
		powerUp.acquirePower(this);
		this.mario.setHealth(this.mario.getMaxHealth());
		this.powerUp = powerUp;
		this.mario.calculateDimensions();
	}
	public ParsedCharacter getCharacter() {
		return character;
	}
	@Override public Identifier getCharacterID() {
		return character.ID;
	}
	public void setCharacter(ParsedCharacter character) {
		this.character = character;
		this.mario.calculateDimensions();
	}
}
