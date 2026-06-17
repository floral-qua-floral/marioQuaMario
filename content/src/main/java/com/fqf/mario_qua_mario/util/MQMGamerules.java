package com.fqf.mario_qua_mario.util;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.world.GameRules;

public class MQMGamerules {
//	public static int coinsForPowerUp;

	public static final GameRules.Key<GameRules.BooleanRule> ALLOW_RISING_STOMPS =
			GameRuleRegistry.register("mqmDoRisingStomps", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<DoubleRule> INCOMING_DAMAGE_MULTIPLIER =
			GameRuleRegistry.register("mqmIncomingDamageMultiplier", GameRules.Category.PLAYER,
					GameRuleFactory.createDoubleRule(2.5));

//	public static final GameRules.Key<GameRules.IntRule> COINS_FOR_POWER_UP =
//			GameRuleRegistry.register("mqmCoinsForPowerUp", GameRules.Category.PLAYER,
//					GameRuleFactory.createIntRule(8, 0, 64, (server, intRule) -> {
//
//					}));

//	public static final GameRules.Key<DoubleRule> GROUND_POUND_SOLID_MULTIPLIER =
//			GameRuleRegistry.register("mqmGroundPoundSolidEntityDamageMultiplier", GameRules.Category.PLAYER,
//					GameRuleFactory.createDoubleRule(3));

//	public static final GameRules.Key<DoubleRule> OUTGOING_DAMAGE_MULTIPLIER =
//			GameRuleRegistry.register("marioOutgoingDamageMultiplier", GameRules.Category.PLAYER,
//					GameRuleFactory.createDoubleRule(1.0));

	public static void register() {

	}
}
