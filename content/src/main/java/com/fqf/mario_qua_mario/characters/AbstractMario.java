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
import com.fqf.mario_qua_mario.util.MarioVars;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractMario implements CharacterDefinition {
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
		if(source.getTypeRegistryEntry().matchesKey(DamageTypes.LAVA)) {
			data.forceActionTransition(Debug.ID, LavaBoost.ID);
			return 10;
		}

		return amount * (float) data.getPlayer().getWorld().getGameRules().get(MQMGamerules.INCOMING_DAMAGE_MULTIPLIER).get();
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
	@Override public float getAnimationWidthFactor() {
		return 1;
	}
	@Override public float getAnimationHeightFactor() {
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
				new AttributeModifierInstruction(EntityAttributes.GENERIC_ATTACK_SPEED, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
		);
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new MarioVars();
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
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		commonTick(data);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		commonTick(data);
		data.retrieveStateData(MarioVars.class).stompGuardRemainingTicks--;
	}
}
