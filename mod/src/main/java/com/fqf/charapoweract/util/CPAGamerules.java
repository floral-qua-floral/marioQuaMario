package com.fqf.charapoweract.util;

import com.fqf.charapoweract.packets.CPAPackets;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

public class CPAGamerules {
	public static boolean useCharacterStats;
	public static boolean restrictAdventureBapping;
	public static boolean adventurePlayersBreakBrittleBlocks;

	public static final GameRules.Key<GameRules.BooleanRule> USE_CHARACTER_STATS =
			GameRuleRegistry.register("cpaUseCharacterStatModifiers", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						useCharacterStats = booleanRule.get();
						CPAPackets.syncUseCharacterStatsS2C(server, useCharacterStats);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> RESTRICT_ADVENTURE_BAPPING =
			GameRuleRegistry.register("cpaRestrictBapsInAdventureMode", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						restrictAdventureBapping = booleanRule.get();
						syncAdventureRules(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> ADVENTURE_PLAYERS_BREAK_BRITTLE_BLOCKS =
			GameRuleRegistry.register("cpaBreakBrittleBlocksInAdventureMode", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false, (server, booleanRule) -> {
						adventurePlayersBreakBrittleBlocks = booleanRule.get();
						syncAdventureRules(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> PETS_AND_TEAMMATES_RESIST_COLLISION_ATTACKS =
			GameRuleRegistry.register("cpaFriendliesResistCollisionAttacks", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						adventurePlayersBreakBrittleBlocks = booleanRule.get();
						syncAdventureRules(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> REJECT_INVALID_ACTION_TRANSITIONS =
			GameRuleRegistry.register("cpaRejectInvalidActionTransitions", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> DISABLE_CHARACTER_MOVEMENT_CHECK =
			GameRuleRegistry.register("cpaDisableCharacterMovementCheck", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

//	public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_PLAYERMODELS =
//			GameRuleRegistry.register("cpaRequirePlayermodels", GameRules.Category.PLAYER,
//					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> REVERT_TO_SMALL =
			GameRuleRegistry.register("cpaAlwaysRevertToWeakestForm", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

	private static void syncAdventureRules(MinecraftServer server) {
		CPAPackets.syncRestrictAdventureBapsS2C(server, restrictAdventureBapping, adventurePlayersBreakBrittleBlocks);
	}

	public static void register() {

	}
}
