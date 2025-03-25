package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.registries.ParsedMarioState;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class MarioPlayerData implements IMarioReadableMotionData {
	protected MarioPlayerData() {
		MarioQuaMario.LOGGER.info("Created new MarioData: {}", this);
//		this.disable();

//		this.character = Objects.requireNonNull(RegistryManager.CHARACTERS.get(MarioQuaMario.makeID("mario")),
//				"Mario isn't registered; can't initialize player!");
//		this.action = this.character.INITIAL_ACTION;
//		this.powerUp = this.character.INITIAL_POWER_UP;

//		this.action = RegistryManager.ACTIONS.get(MarioQuaMario.makeID("debug"));
//		this.setActionTransitionlessInternal(this.action);
//		this.powerUp = null;
//		this.character = null;
	}

	private static final Identifier FALL_RESISTANCE_ID = MarioQuaMario.makeID("mario_fall_resistance");
	private static final EntityAttributeModifier FALL_RESISTANCE = new EntityAttributeModifier(
			FALL_RESISTANCE_ID, 8, EntityAttributeModifier.Operation.ADD_VALUE
	);

	private static final Identifier ATTACK_SLOWDOWN_ID = MarioQuaMario.makeID("mario_fall_resistance");
	private static final EntityAttributeModifier ATTACK_SLOWDOWN = new EntityAttributeModifier(
			ATTACK_SLOWDOWN_ID, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);
	@Override public boolean isEnabled() {
		return this.character != null;
	}
	public void disableInternal() {
		this.character = null;
		this.powerUp = null;
		this.action = null;
		this.updatePassiveUniversalTraits(false);
	}
	public void updatePassiveUniversalTraits(boolean enabled) {
		EntityAttributeInstance safeFallAttributeInstance = this.getMario().getAttributeInstance(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
		EntityAttributeInstance attackSpeedAttributeInstance = this.getMario().getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		assert safeFallAttributeInstance != null && attackSpeedAttributeInstance != null;
		safeFallAttributeInstance.removeModifier(FALL_RESISTANCE_ID);
		attackSpeedAttributeInstance.removeModifier(ATTACK_SLOWDOWN_ID);
		if(enabled) { // TODO: These should be handled by the character rather than being universal!
			safeFallAttributeInstance.addPersistentModifier(FALL_RESISTANCE);
			attackSpeedAttributeInstance.addPersistentModifier(ATTACK_SLOWDOWN);
		}
	}

	private AbstractParsedAction action;
	public boolean resetAnimation;
	public PlayermodelAnimation prevAnimation;
	public boolean tickAnimation = true;
	public AbstractParsedAction getAction() {
		return this.action;
	}
	@Override public Identifier getActionID() {
		return this.isEnabled() ? this.getAction().ID : null;
	}
	@Override public ActionCategory getActionCategory() {
		return this.getAction().CATEGORY;
	}

	public boolean setAction(@Nullable AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, boolean forced, boolean fromCommand) {
		boolean transitionedNaturally = ParsedActionHelper.attemptTransitionTo(this, fromAction == null ? this.getAction() : fromAction, toAction, seed);
		if(transitionedNaturally && this instanceof MarioMoveableData moveableData) moveableData.applyModifiedVelocity();
		else if(forced) this.setActionTransitionless(toAction);
		return transitionedNaturally || forced;
	}
	public void setActionTransitionless(AbstractParsedAction action) {
		this.resetAnimation = true;
		this.prevAnimation = this.action == null ? null : this.action.ANIMATION;
		this.setupCustomVars(this.action, action);
		this.action = action;
		this.getMario().calculateDimensions();
//		if(action.CATEGORY != ActionCategory.MOUNTED) this.getMario().dismountVehicle();
	}

	private ParsedPowerUp powerUp;
	public ParsedPowerUp getPowerUp() {
		return this.powerUp;
	}
	@Override public Identifier getPowerUpID() {
		return this.isEnabled() ? this.getPowerUp().ID : null;
	}

	public boolean setPowerUp(ParsedPowerUp newPowerUp, boolean isReversion, long seed) {
		return this.setPowerUpTransitionless(newPowerUp);
	}
	public boolean setPowerUpTransitionless(ParsedPowerUp newPowerUp) {
		this.setupCustomVars(this.powerUp, newPowerUp);
		this.powerUp = newPowerUp;
		updateCharacterFormCombo();
		return true;
	}

	private ParsedCharacter character;
	public ParsedCharacter getCharacter() {
		return this.character;
	}
	@Override public Identifier getCharacterID() {
		return this.isEnabled() ? this.getCharacter().ID : null;
	}

	public void setCharacter(ParsedCharacter character) {
		this.setupCustomVars(this.character, character);
		this.character = character;
		this.powerUp = character.INITIAL_POWER_UP;
		this.action = character.INITIAL_ACTION;
		this.setActionTransitionless(character.INITIAL_ACTION);
		this.setPowerUpTransitionless(character.INITIAL_POWER_UP);
		updateCharacterFormCombo();
		this.updatePassiveUniversalTraits(true);
	}

	public void setupCustomVars(ParsedMarioState oldThing, ParsedMarioState newThing) {
		Object newThingVars = newThing.makeCustomThing(this);
		Class<?> newThingVarsClass = newThingVars == null ? null : newThingVars.getClass();
		Class<?> oldThingVarsClass = oldThing == null ? null : oldThing.getLastCustomVarsClass();

		if(newThingVarsClass != null)
			this.customVars.put(newThingVarsClass, newThingVars);

		if(oldThingVarsClass != null && !oldThingVarsClass.equals(newThingVarsClass))
			this.customVars.remove(oldThingVarsClass); // If we didn't already just replace the vars, delete the old ones
	}
	private final Set<String> POWERS = new HashSet<>();
	public void updateCharacterFormCombo() {
		this.POWERS.clear();
		this.POWERS.addAll(this.getPowerUp().POWERS);
		this.POWERS.addAll(this.getCharacter().POWERS);
		this.getMario().calculateDimensions();
	}
	@Override public boolean hasPower(String power) {
		return this.isEnabled() && this.POWERS.contains(power);
	}

	public void setupVariablesBeforeInitialApply(ParsedCharacter character, ParsedPowerUp powerUp) {
		this.character = character;
		this.powerUp = powerUp;
	}
	public void initialApply() {
		this.disableInternal();
	}

	public void tick() {
		this.tickAnimation = true;
	}

	@Override public double getStat(CharaStat stat) {
		return this.getPowerUp().adjustStat(stat, this.getCharacter().adjustStat(stat, stat.BASE_VALUE));
	}

	@Override public float getHorizontalScale() {
		return this.isEnabled() ? this.getPowerUp().WIDTH_FACTOR * this.getCharacter().WIDTH_FACTOR : 1;
	}
	@Override public float getVerticalScale() {
		return this.isEnabled() ? this.getPowerUp().HEIGHT_FACTOR * this.getCharacter().HEIGHT_FACTOR : 1;
	}

	@Override public int getBumpStrengthModifier() {
		return this.getPowerUp().BUMP_STRENGTH_MODIFIER + this.getCharacter().BUMP_STRENGTH_MODIFIER;
	}

	private final Map<Class<?>, Object> customVars = new HashMap<>();
	@Override public <T> T getVars(Class<T> clazz) {
		Object customData = this.customVars.get(clazz);
		return clazz.cast(customData);
	}

	public boolean doMarioTravel() {
		return this.isEnabled() && !this.getMario().getAbilities().flying && !this.getMario().isFallFlying() && !this.getMario().isUsingRiptide() && !this.getMario().isClimbing();
	}

	public Vec3d getFluidPushingVel() {
		Box box = this.getMario().getBoundingBox().contract(0.001);
		int boxMinX = MathHelper.floor(box.minX); int boxMaxX = MathHelper.ceil(box.maxX);
		int boxMinY = MathHelper.floor(box.minY); int boxMaxY = MathHelper.ceil(box.maxY);
		int boxMinZ = MathHelper.floor(box.minZ); int boxMaxZ = MathHelper.ceil(box.maxZ);

		BlockPos.Mutable mutable = new BlockPos.Mutable();
		double velX = 0; double velY = 0; double velZ = 0; int fluidsCount = 0;
		World world = this.getMario().getWorld();
		for (int checkX = boxMinX; checkX < boxMaxX; checkX++) {
			for (int checkY = boxMinY; checkY < boxMaxY; checkY++) {
				for (int checkZ = boxMinZ; checkZ < boxMaxZ; checkZ++) {
					mutable.set(checkX, checkY, checkZ);
					FluidState fluidState = world.getFluidState(mutable);
					double e = (float) checkY + fluidState.getHeight(world, mutable);
					if (e >= box.minY) {
						Vec3d fluidVelocity = fluidState.getVelocity(world, mutable);
						double factor = 1.8;
						int tickRate = fluidState.getFluid().getTickRate(world);
						if(tickRate == 0) continue;
						fluidsCount++;
						velX += fluidVelocity.x * factor / tickRate;
						velY += fluidVelocity.y * factor / tickRate;
						velZ += fluidVelocity.z * factor / tickRate;
					}
				}
			}
		}
		if(fluidsCount > 0) {
			velX /= fluidsCount;
			velY /= fluidsCount;
			velZ /= fluidsCount;
		}

		return new Vec3d(velX, velY, velZ);
	}

	public HeadRestrictionType headRestricted;
	public enum HeadRestrictionType {
		NONE,
		NORMAL,
		URGENT
	}
	@Override
	public void forceBodyAlignment(boolean urgent) {
		this.headRestricted = urgent ? HeadRestrictionType.URGENT : HeadRestrictionType.NORMAL;
	}

	public DismountType attemptDismount;
	public enum DismountType {
		REMAIN_MOUNTED,
		DISMOUNT_IN_PLACE,
		VANILLA_DISMOUNT
	}
}
