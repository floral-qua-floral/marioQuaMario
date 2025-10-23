package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;
import java.util.Objects;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;
import static java.util.EnumSet.of;
import static net.minecraft.entity.attribute.EntityAttributes.*;

public class CharaStatCalculationHelper {
	public static double calculate(MarioPlayerData data, CharaStat stat) {
		return data.getPowerUp().adjustStat(stat, data.getCharacter().adjustStat(stat, adjustBaseValue(data, stat)));
	}

//	private static final EnumSet<StatCategory> AFFECTED_BY_MOVEMENT_EFFICIENCY = of(WALKING);

	private static final Identifier SPRINTING_ATTRIBUTE_MODIFIER_ID = Identifier.ofVanilla("sprinting");

	public static double adjustBaseValue(MarioPlayerData data, CharaStat stat) {
		PlayerEntity mario = data.getMario();

		double multiplier = 1;

		if(containsAny(stat.CATEGORIES, SPEED, ACCELERATION)) {
			if(containsAny(stat.CATEGORIES, WALKING, RUNNING, P_RUNNING)) {
				EntityAttributeInstance moveSpeedAttribute = mario.getAttributeInstance(GENERIC_MOVEMENT_SPEED);
				assert moveSpeedAttribute != null;

				EntityAttributeModifier sprintingModifier = moveSpeedAttribute.getModifier(SPRINTING_ATTRIBUTE_MODIFIER_ID);
				if(sprintingModifier != null) moveSpeedAttribute.removeModifier(sprintingModifier);

				float marioSpeed = mario.getMovementSpeed(); // floating point inaccuracy worth it to cooperate w/ weird mixins?
				multiplier *= (marioSpeed / mario.getAttributeBaseValue(GENERIC_MOVEMENT_SPEED));

				if(sprintingModifier != null) moveSpeedAttribute.addTemporaryModifier(sprintingModifier);
			}

			if(stat.CATEGORIES.contains(SWIMMING)) {
				multiplier *= (1 + mario.getAttributeValue(GENERIC_WATER_MOVEMENT_EFFICIENCY));
			}
		}

		if(mario.hasStatusEffect(StatusEffects.LEVITATION)) {
			if(containsAny(stat.CATEGORIES, NORMAL_GRAVITY, JUMPING_GRAVITY, AQUATIC_GRAVITY)) {
				multiplier = 0;
			}
		}
		else {
			if(containsAny(stat.CATEGORIES, NORMAL_GRAVITY, JUMPING_GRAVITY, AQUATIC_GRAVITY)) {
				double gravity = mario.getFinalGravity();
				multiplier *= gravity / mario.getAttributeBaseValue(GENERIC_GRAVITY);

				if(data.getYVel() < 0 && mario.hasStatusEffect(StatusEffects.SLOW_FALLING))
					multiplier *= 0.125;
			}

			if(stat.CATEGORIES.contains(TERMINAL_VELOCITY) && !stat.CATEGORIES.contains(STOMP)) {
				if(mario.hasStatusEffect(StatusEffects.SLOW_FALLING))
					multiplier *= 0.4;
			}
		}

		if(stat.CATEGORIES.contains(JUMP_VELOCITY)) {
			multiplier *= mario.getAttributeValue(GENERIC_JUMP_STRENGTH) / mario.getAttributeBaseValue(GENERIC_JUMP_STRENGTH);
			StatusEffectInstance jumpBoost = mario.getStatusEffect(StatusEffects.JUMP_BOOST);
			if(jumpBoost != null) {
				multiplier *= 1 + 0.125 * (jumpBoost.getAmplifier() + 1);
			}
		}

		if(Math.abs(1 - multiplier) <= MathHelper.EPSILON) return stat.BASE_VALUE;
		return stat.BASE_VALUE * multiplier;
	}

	private static boolean containsAny(EnumSet<StatCategory> categories, StatCategory... anyOf) {
		for(StatCategory checkFor : anyOf) {
			if(categories.contains(checkFor)) return true;
		}
		return false;
	}
}
