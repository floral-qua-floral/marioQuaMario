package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.packets.MarioPackets;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

public class MarioGamerules {
	public static boolean useCharacterStats;
	public static boolean restrictAdventureBapping;
	public static boolean adventurePlayersBreakBrittleBlocks;

	public static final GameRules.Key<GameRules.BooleanRule> USE_CHARACTER_STATS =
			GameRuleRegistry.register("mqmUseCharacterStats", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						useCharacterStats = booleanRule.get();
						MarioPackets.syncUseCharacterStatsS2C(server, useCharacterStats);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> RESTRICT_ADVENTURE_BAPPING =
			GameRuleRegistry.register("mqmRestrictBapsInAdventureMode", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						restrictAdventureBapping = booleanRule.get();
						syncAdventureRules(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> ADVENTURE_PLAYERS_BREAK_BRITTLE_BLOCKS =
			GameRuleRegistry.register("mqmBreakBrittleBlocksInAdventureMode", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false, (server, booleanRule) -> {
						adventurePlayersBreakBrittleBlocks = booleanRule.get();
						syncAdventureRules(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> PETS_AND_TEAMMATES_RESIST_COLLISION_ATTACKS =
			GameRuleRegistry.register("mqmFriendliesResistCollisionAttacks", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						adventurePlayersBreakBrittleBlocks = booleanRule.get();
						syncAdventureRules(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> REJECT_INVALID_ACTION_TRANSITIONS =
			GameRuleRegistry.register("mqmRejectInvalidActionTransitions", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> DISABLE_CHARACTER_MOVEMENT_CHECK =
			GameRuleRegistry.register("mqmDisableCharacterMovementCheck", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

//	public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_PLAYERMODELS =
//			GameRuleRegistry.register("mqmRequirePlayermodels", GameRules.Category.PLAYER,
//					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> REVERT_TO_SMALL =
			GameRuleRegistry.register("mqmAlwaysRevertToWeakestForm", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

	private static void syncAdventureRules(MinecraftServer server) {
		MarioPackets.syncRestrictAdventureBapsS2C(server, restrictAdventureBapping, adventurePlayersBreakBrittleBlocks);
	}

	public static void register() {

	}
}
