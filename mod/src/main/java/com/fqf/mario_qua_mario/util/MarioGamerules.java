package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.packets.MarioPackets;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.world.GameRules;

public class MarioGamerules {
	public static boolean useCharacterStats;

	public static final GameRules.Key<GameRules.BooleanRule> USE_CHARACTER_STATS =
			GameRuleRegistry.register("mqmUseCharacterStats", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						useCharacterStats = booleanRule.get();
						MarioPackets.syncUseCharacterStatsS2C(server, useCharacterStats);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> REJECT_INVALID_ACTION_TRANSITIONS =
			GameRuleRegistry.register("mqmRejectInvalidActionTransitions", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_PLAYERMODELS =
			GameRuleRegistry.register("mqmRequirePlayermodels", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> REVERT_TO_SMALL =
			GameRuleRegistry.register("mqmAlwaysRevertToWeakestForm", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

	public static void register() {

	}
}
