package com.fqf.mario_qua_mario_content.characters;

import com.fqf.mario_qua_mario_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.LavaBoost;
import com.fqf.mario_qua_mario_content.actions.generic.Debug;
import com.fqf.mario_qua_mario_content.util.MarioContentGamerules;
import com.fqf.mario_qua_mario_content.util.MarioVars;
import com.fqf.mario_qua_mario_content.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMario implements CharacterDefinition {
	@Override public @NotNull Identifier getInitialAction() {
		return MarioQuaMarioContent.makeID("fall");
	}
	@Override public @NotNull Identifier getInitialPowerUp() {
		return MarioQuaMarioContent.makeID("super");
	}

	@Override public @NotNull Identifier getMountedAction(Entity vehicle) {
		return MarioQuaMarioContent.makeID("mounted");
	}

	@Override
	public float modifyIncomingDamage(IMarioAuthoritativeData data, DamageSource source, float amount) {
		if(source.getTypeRegistryEntry().matchesKey(DamageTypes.LAVA)) {
			data.forceActionTransition(Debug.ID, LavaBoost.ID);
			return 10;
		}

		float increasedAmount = amount * (float) data.getMario().getWorld().getGameRules().get(MarioContentGamerules.INCOMING_DAMAGE_MULTIPLIER).get();

		// Awful disgusting hack to prevent Poison from being able to kill. We have to try and guess whether this is poison
		boolean isProbablyPoison = (
				amount == 1
				&& source.isOf(DamageTypes.MAGIC)
				&& data.getMario().hasStatusEffect(StatusEffects.POISON)
		);
		return (isProbablyPoison && increasedAmount > data.getMario().getHealth() ? 1 : increasedAmount);
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 1;
	}
	@Override public float getAnimationWidthFactor() {
		return 1;
	}
	@Override public float getAnimationHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
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

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new MarioVars();
	}
	private static void commonTick(IMarioData data) {
		MarioVars vars = data.getVars(MarioVars.class);
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
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		commonTick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {
		commonTick(data);
		data.getVars(MarioVars.class).stompGuardRemainingTicks--;
	}
}
