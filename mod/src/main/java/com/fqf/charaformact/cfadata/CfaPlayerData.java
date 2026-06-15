package com.fqf.charaformact.cfadata;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.ParsedCommonAppearance;
import com.fqf.charaformact.registries.actions.parsed.ParsedWallboundAction;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact.util.CfaStatCalculationHelper;
import com.fqf.charaformact.util.DirectionBasedWallInfo;
import com.fqf.charaformact.util.AdvancedWallInfo;
import com.fqf.charaformact_api.definitions.states.StatAlteringStateDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.WallBodyAlignment;
import com.fqf.charaformact.registries.ParsedCfaState;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.ParsedActionHelper;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.util.CfaStat;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class CfaPlayerData implements CfaReadableMotionData {
	protected CfaPlayerData() {
		CharaFormAct.LOGGER.info("Created new CFA Player Data: {}", this);
	}
	@Override public boolean isEnabled() {
		return this.character != null;
	}
	public void disableInternal() {
		if(this.action != null) this.action.onExit(this);
		if(this.form != null) this.form.onExit(this);
		if(this.character != null) this.character.onExit(this);
		this.character = null;
		this.form = null;
		this.action = null;
		this.updateCharacterFormCombo();
		this.updatePassiveUniversalTraits(false);
	}
	public void updatePassiveUniversalTraits(boolean enabled) {
	}

	private AbstractParsedAction action;
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
		if(transitionedNaturally && this instanceof CfaMoveableData moveableData) moveableData.applyModifiedVelocity();
		else if(forced) this.setActionTransitionless(toAction);
		return transitionedNaturally || forced;
	}
	public void setActionTransitionless(AbstractParsedAction action) {
		this.setupCustomVars(this.action, action);
		this.action.onExit(this);
		this.action = action;
		this.action.onEnter(this);
		this.getPlayer().calculateDimensions();
//		if(action.CATEGORY != ActionCategory.MOUNTED) this.getPlayer().dismountVehicle();
	}

	private ParsedForm form;
	public ParsedForm getForm() {
		return this.form;
	}
	@Override public Identifier getFormID() {
		return this.isEnabled() ? this.getForm().ID : null;
	}

	@Override
	public int getFormPriority() {
		return this.isEnabled() ? this.getForm().VALUE : -1;
	}

	public boolean setForm(ParsedForm newForm, boolean isReversion, long seed) {
		return this.setFormTransitionless(newForm);
	}
	public boolean setFormTransitionless(ParsedForm newForm) {
		this.setupCustomVars(this.form, newForm);
		this.form.onExit(this);
		this.form = newForm;
		this.form.onEnter(this);
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
		if(this.character != null) this.character.onExit(this);
		this.character = character;
		this.character.onEnter(this);
		this.form = character.INITIAL_FORM;
		this.action = character.getInitialAction(this);
		this.setActionTransitionless(this.action);
		this.setFormTransitionless(character.INITIAL_FORM);
		updateCharacterFormCombo();
		this.updatePassiveUniversalTraits(true);
	}

	public void setupCustomVars(ParsedCfaState oldThing, ParsedCfaState newThing) {
		Object newThingVars = newThing.makeCustomThing(this);
		Class<?> newThingVarsClass = newThingVars == null ? null : newThingVars.getClass();
		Class<?> oldThingVarsClass = oldThing == null ? null : oldThing.getLastCustomVarsClass();

		if(newThingVarsClass != null)
			this.customVars.put(newThingVarsClass, newThingVars);

		if(oldThingVarsClass != null && !oldThingVarsClass.equals(newThingVarsClass))
			this.customVars.remove(oldThingVarsClass); // If we didn't already just replace the vars, delete the old ones
	}

	private final Set<String> POWERS = new HashSet<>();
	private final List<StatAlteringStateDefinition.AttributeModifierInstruction> ATTRIBUTE_MODIFIERS = new ArrayList<>();
	private float horizontalScale, verticalScale, eyeHeightScale, horizontalAnimationScale, verticalAnimationScale;

	public float getHorizontalScale() {
		return this.horizontalScale;
	}
	public float getVerticalScale() {
		return this.verticalScale;
	}
	public float getEyeHeightScale() {
		return this.eyeHeightScale;
	}
	public float getHorizontalAnimationScale() {
		return this.horizontalAnimationScale;
	}
	public float getVerticalAnimationScale() {
		return this.verticalAnimationScale;
	}

	public void updateCharacterFormCombo() {
		this.updateAppearance();

		// Clear all power strings
		this.POWERS.clear();

		// Remove all attribute modifiers
		int removingIndex = 0;
		for(StatAlteringStateDefinition.AttributeModifierInstruction removeModifier : this.ATTRIBUTE_MODIFIERS) {
			EntityAttributeInstance attributeInstance = this.getPlayer().getAttributeInstance(removeModifier.attribute());
			if(attributeInstance == null) {
				CharaFormAct.LOGGER.error("Trying to remove a generated attribute modifier from attribute {}, however" +
						" the player has no instance of this attribute?!\n\tPlayer: {}",
						removeModifier.attribute().getIdAsString(),
						this.getPlayer());
				removingIndex++;
			}
			else {
				attributeInstance.removeModifier(CharaFormAct.makeID("generated_modifier_" + removingIndex++));
			}
		}

		// Clear attribute modifier instructions
		this.ATTRIBUTE_MODIFIERS.clear();

		if(this.isEnabled()) {
			// Store new power strings
			this.POWERS.addAll(this.getCharacter().POWERS);
			this.POWERS.addAll(this.getForm().POWERS);

			// Store new attribute modifier instructions
			this.ATTRIBUTE_MODIFIERS.addAll(this.getCharacter().ATTRIBUTE_MODIFIERS);
			this.ATTRIBUTE_MODIFIERS.addAll(this.getForm().ATTRIBUTE_MODIFIERS);

			// Create new attribute modifiers
			int addingIndex = 0;
			for(StatAlteringStateDefinition.AttributeModifierInstruction addModifier : this.ATTRIBUTE_MODIFIERS) {
				EntityAttributeInstance attributeInstance = this.getPlayer().getAttributeInstance(addModifier.attribute());
				if(attributeInstance == null) {
					CharaFormAct.LOGGER.error("Trying to add a generated attribute modifier to attribute {}, however" +
									" player {} has no instance of this attribute?!",
							addModifier.attribute().getIdAsString(),
							this.getPlayer());
					addingIndex++;
				}
				else {
					// FIXME: Error on client when changing world >:(
					attributeInstance.addTemporaryModifier(new EntityAttributeModifier(
							CharaFormAct.makeID("generated_modifier_" + addingIndex++),
							addModifier.d(),
							addModifier.operation()
					));
				}
			}

			this.horizontalScale = this.getForm().WIDTH_FACTOR * this.getCharacter().WIDTH_FACTOR;
			this.verticalScale = this.getForm().HEIGHT_FACTOR * this.getCharacter().HEIGHT_FACTOR;
			this.eyeHeightScale = this.getForm().HEIGHT_FACTOR * this.getCharacter().EYE_HEIGHT_FACTOR;
			this.horizontalAnimationScale = this.getForm().ANIMATION_HORIZONTAL_SCALE * this.getCharacter().ANIMATION_HORIZONTAL_SCALE;
			this.verticalAnimationScale = this.getForm().ANIMATION_VERTICAL_SCALE * this.getCharacter().ANIMATION_VERTICAL_SCALE;
		}
		else {
			this.horizontalScale = 1; this.verticalScale = 1;
			this.eyeHeightScale = 1;
			this.horizontalAnimationScale = 1; this.verticalAnimationScale = 1;
		}

		this.getPlayer().calculateDimensions();
	}
	@Override public boolean hasPower(String power) {
		return this.isEnabled() && this.POWERS.contains(power);
	}

	public abstract void updateAppearance();
	public abstract @Nullable ParsedCommonAppearance getAppearance();

	public void setupVariablesBeforeInitialApply(ParsedCharacter character, ParsedForm form) {
		this.character = character;
		this.form = form;
		this.action = character.getInitialAction(this);
	}
	public void initialApply() {
		this.disableInternal();
	}

	public void tick() {
		this.tickAnimation = true;
		this.onLookAround();
	}

	@Override public double getStat(CfaStat stat) {
		return CfaStatCalculationHelper.calculate(this, stat);
	}

	@Override public int getBapStrength(Direction direction) {
		return this.getBapStrength(this.getAction(), direction);
	}

	public int getBapStrength(AbstractParsedAction action, Direction direction) {
		int actionBapStrength = switch(direction) {
			case DOWN -> action.BAPPING_RULE.floorBumpStrength();
			case UP -> action.BAPPING_RULE.ceilingBumpStrength();
			case NORTH, SOUTH, WEST, EAST -> action.BAPPING_RULE.wallBumpStrength();
		};
		if(actionBapStrength <= 1) return Math.max(0, actionBapStrength);
		return Math.max(1, actionBapStrength + this.getCharacter().BUMP_STRENGTH_MODIFIER + this.getForm().BUMP_STRENGTH_MODIFIER);
	}

	private final Map<Class<?>, Object> customVars = new HashMap<>();
	@Override public <T> T retrieveStateData(Class<T> clazz) {
		Object customData = this.customVars.get(clazz);
		return clazz.cast(customData);
	}

	public boolean doCustomTravel() {
		return
				this.isEnabled()
				&& !this.getPlayer().getAbilities().flying // this means "currently flying", not "can fly"
				&& !this.getPlayer().isFallFlying()
				&& !this.getPlayer().isUsingRiptide() // do i want to keep this here?
				&& !this.getPlayer().isSleeping();
	}

	public Vec3d getFluidPushingVel() {
		Box box = this.getPlayer().getBoundingBox().contract(0.001);
		int boxMinX = MathHelper.floor(box.minX); int boxMaxX = MathHelper.ceil(box.maxX);
		int boxMinY = MathHelper.floor(box.minY); int boxMaxY = MathHelper.ceil(box.maxY);
		int boxMinZ = MathHelper.floor(box.minZ); int boxMaxZ = MathHelper.ceil(box.maxZ);

		BlockPos.Mutable mutable = new BlockPos.Mutable();
		double velX = 0; double velY = 0; double velZ = 0; int fluidsCount = 0;
		World world = this.getPlayer().getWorld();
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

		return new Vec3d(velX, Math.max(velY, -0.15), velZ);
	}

	public HeadRestrictionType headRestricted;

	protected final DirectionBasedWallInfo INPUTLESS_WALL_INFO = new DirectionBasedWallInfo(this) {
		@Override public double getTowardsWallInput() {
			return 0;
		}
		@Override public double getSidleInput() {
			return 0;
		}
	};
	public AdvancedWallInfo getWallInfo() {
		return this.INPUTLESS_WALL_INFO;
	}

	public enum HeadRestrictionType {
		NONE,
		NORMAL,
		URGENT
	}
	@Override
	public void forceBodyAlignment(boolean urgent) {
		this.headRestricted = urgent ? HeadRestrictionType.URGENT : HeadRestrictionType.NORMAL;
	}

	private @Nullable Object2DoubleMap.Entry<TagKey<Fluid>> getHighestFluid() {
		Object2DoubleMap.Entry<TagKey<Fluid>> highestFluid = null;
		for(Object2DoubleMap.Entry<TagKey<Fluid>> entry : this.getPlayer().fluidHeight.object2DoubleEntrySet()) {
			if(entry.getDoubleValue() == 0) continue;

			if(highestFluid == null || entry.getDoubleValue() > highestFluid.getDoubleValue())
				highestFluid = entry;
		}
		return highestFluid;
	}

	@Override
	public double getImmersionLevel() {
		Object2DoubleMap.Entry<TagKey<Fluid>> highestFluid = this.getHighestFluid();
		return highestFluid == null ? 0 : highestFluid.getDoubleValue();
	}

	@Override
	public double getImmersionPercent() {
		return Math.min(this.getImmersionLevel() / this.getPlayer().getHeight(), 1);
	}

	@Override
	public boolean isOnGround() {
		return this.getPlayer().isOnGround();
	}

	@Override
	public boolean isNearGround(double range) {
		return this.getSolidDistance(range, Direction.DOWN) < range;
	}

	@Override
	public double getSolidDistance(double maxDistance, Direction direction) {
		// This isn't very optimized(?) but I don't care that much TBH
		Direction.AxisDirection axisDir = direction.getDirection();
		return Math.abs(Entity.adjustMovementForCollisions(
				this.getPlayer(),
				Vec3d.ZERO.withAxis(direction.getAxis(), maxDistance * axisDir.offset()),
				this.getPlayer().getBoundingBox(),
				this.getPlayer().getWorld(),
				List.of()
		).getComponentAlongAxis(direction.getAxis()));
	}

	public void onLookAround() {
		if(!this.isEnabled() || this.getActionCategory() != ActionCategory.WALLBOUND) return;

		PlayerEntity player = this.getPlayer();
		ParsedWallboundAction wallAction = (ParsedWallboundAction) this.getAction();

		if(wallAction.ALIGNMENT == WallBodyAlignment.ANY) return;

		float wallYaw = this.getWallInfo().getWallYaw();

		float bodyYaw = wallYaw + switch (wallAction.ALIGNMENT) {
			case TOWARDS -> 0;
			case AWAY -> 180;
			case SIDEWAYS -> this.getWallInfo().getYawDeviation() > 0 ? -90 : 90;
			default -> throw new IllegalStateException("Unexpected value: " + wallAction.ALIGNMENT);
		};

		player.setBodyYaw(bodyYaw);

		if(wallAction.HEAD_RANGE >= 360) return;

		float headDifference = MathHelper.wrapDegrees(player.getYaw() - bodyYaw);
		float clampedHeadYawDifference = MathHelper.clamp(headDifference, -wallAction.HEAD_RANGE, wallAction.HEAD_RANGE);
		player.prevYaw += clampedHeadYawDifference - headDifference;
		player.setYaw(player.getYaw() + clampedHeadYawDifference - headDifference);

		player.setHeadYaw(player.getYaw());
	}
}
