package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.LavaBoost;
import com.fqf.mario_qua_mario.actions.generic.Debug;
import com.fqf.mario_qua_mario.actions.mounted.Mounted;
import com.fqf.mario_qua_mario.forms.Super;
import com.fqf.mario_qua_mario.util.MQMGamerules;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.MarioVars;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractMarioSeriesCharacter implements CharacterDefinition {
	@Override public @NotNull Identifier getInitialAction() {
		return Fall.ID;
	}
	@Override public @NotNull Identifier getInitialForm() {
		return Super.ID;
	}

	@Override public @NotNull Identifier getMountedAction(Entity vehicle) {
		return Mounted.ID;
	}

	@Override
	public float modifyIncomingDamage(CfaAuthoritativeData data, DamageSource source, float amount) {
		float multiplier = (float) data.getPlayer().getWorld().getGameRules().get(MQMGamerules.INCOMING_DAMAGE_MULTIPLIER).get();

		if(source.isIn(MQMTags.TRIGGERS_LAVA_BOOST)) {
			// See if we can find a spot to eject Mario to?
			Vec3d ejectionLocation = LavaBoost.findLavaBoostEjectionSpot(data);

			if(ejectionLocation != null) {
				ServerPlayerEntity player = data.getPlayer();
				player.requestTeleport(player.getX(), player.getY(), player.getZ());
				data.forceActionTransition(Debug.ID, LavaBoost.ID);
				MarioQuaMario.LOGGER.info("Lava boost ejection to {}", ejectionLocation.y);
				return 4 * multiplier;
			}
		}

		return amount * multiplier;
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 1;
	}
	@Override public float getEyeHeightFactor() {
		return 1;
	}
	@Override public float getAnimationHorizontalScale() {
		return 1;
	}
	@Override public float getAnimationVerticalScale() {
		return 1;
	}

	@Override public int getBapStrengthModifier() {
		return 0;
	}

	@Override public Set<String> getPowers() {
		return Set.of(
				Powers.DROP_COINS,
				Powers.LIGHTNING_SHRINKS,
				Powers.CEILING_CLIPPING,
				Powers.STOMP_GUARD
		);
	}

	@Override public Set<AttributeModifierInstruction> getAttributeModifiers() {
		return Set.of(
				new AttributeModifierInstruction(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 8, EntityAttributeModifier.Operation.ADD_VALUE),
				new AttributeModifierInstruction(EntityAttributes.GENERIC_ATTACK_SPEED, 1.7, EntityAttributeModifier.Operation.ADD_VALUE),
				new AttributeModifierInstruction(EntityAttributes.GENERIC_ATTACK_SPEED, -0.7, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
		);
	}

	public static final EntityAttributeModifier UNARMED_DAMAGE_BONUS =
			new EntityAttributeModifier(MarioQuaMario.makeID("unarmed_damage_boost"), 1, EntityAttributeModifier.Operation.ADD_VALUE);
	@Override public @Nullable Object provideStateData(CfaData data) {
		return new MarioVars(data);
	}
	private static void commonTick(CfaData data) {
		MarioVars vars = data.retrieveStateData(MarioVars.class);
		vars.canDoubleJumpTicks--;
		vars.canTripleJumpTicks--;
		switch(data.getActionCategory()) {
			case AIRBORNE -> {
				if(vars.pSpeed < 1)
					vars.pSpeed -= 0.1; // P-Speed decays while airborne, unless it's full
			}
			case AQUATIC ->
					vars.pSpeed -= 0.225; // P-Speed decays faster while waterborne
			default ->
					// P-Speed is wiped while grounded, mounted, on a wall, or in a Generic action
					// Individual actions can change this by assigning a value to P-Speed themselves!
					vars.pSpeed = 0;
		}

		// I wanted to handle this somewhere other than in tick (ideally would be wherever attribute modifiers get
		// applied?) but I could not find that and it seemed not worth the hassle. I'm pretty sure that just happens
		// somewhere in tick too, since it seems like PlayerEntity.getMainHandItem returns based on the index of the
		// selected hotbar slot, which can change Literally Whenever. So it's not as though the attribute modifiers are
		// being applied at the very instant that the player switches slots.
		PlayerEntity mario = data.getPlayer();
		boolean mainHandEmpty = mario.getMainHandStack().isEmpty();
		if(mainHandEmpty == vars.hasUnarmedModifier) return;

		vars.hasUnarmedModifier = mainHandEmpty;
		EntityAttributeInstance attribute = mario.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		assert attribute != null; // if it's ever null i'll cry why on EARTH would it be null jesus christ
		if(mainHandEmpty) attribute.addTemporaryModifier(UNARMED_DAMAGE_BONUS);
		else attribute.removeModifier(UNARMED_DAMAGE_BONUS);
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		commonTick(data);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		commonTick(data);
		data.retrieveStateData(MarioVars.class).stompGuardRemainingTicks--;
	}

	@Override public void onExit(CfaData data) {
		Objects.requireNonNull(data.getPlayer().getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).removeModifier(UNARMED_DAMAGE_BONUS);
	}
}
