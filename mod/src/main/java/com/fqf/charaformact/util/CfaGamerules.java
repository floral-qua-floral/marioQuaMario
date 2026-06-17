package com.fqf.charaformact.util;

import com.fqf.charaformact.packets.CfaPackets;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class CfaGamerules {
	public static boolean useCharacterStats;
	public static int coinsForPowerUp;
	public static boolean restrictAdventureBapping;
	public static boolean adventurePlayersBreakBrittleBlocks;

	public static final GameRules.Key<GameRules.BooleanRule> ALLOW_NULL_APPEARANCE =
			GameRuleRegistry.register("cfaAllowNullAppearance", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false)
			);

	public static final GameRules.Key<GameRules.BooleanRule> USE_CHARACTER_STATS =
			GameRuleRegistry.register("cfaUseCharacterStatModifiers", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						useCharacterStats = booleanRule.get();
						CfaPackets.syncGamerulesS2C(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> RESTRICT_ADVENTURE_BAPPING =
			GameRuleRegistry.register("cfaRestrictBapsInAdventureMode", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						restrictAdventureBapping = booleanRule.get();
						CfaPackets.syncGamerulesS2C(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> ADVENTURE_PLAYERS_BREAK_BRITTLE_BLOCKS =
			GameRuleRegistry.register("cfaBreakBrittleBlocksInAdventureMode", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false, (server, booleanRule) -> {
						adventurePlayersBreakBrittleBlocks = booleanRule.get();
						CfaPackets.syncGamerulesS2C(server);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> PETS_AND_TEAMMATES_RESIST_COLLISION_ATTACKS =
			GameRuleRegistry.register("cfaFriendliesResistCollisionAttacks", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true)
			);

	public static final GameRules.Key<GameRules.BooleanRule> REJECT_INVALID_ACTION_TRANSITIONS =
			GameRuleRegistry.register("cfaRejectInvalidActionTransitions", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> DISABLE_CHARACTER_MOVEMENT_CHECK =
			GameRuleRegistry.register("cfaDisableCharacterMovementCheck", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

//	public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_PLAYERMODELS =
//			GameRuleRegistry.register("cfaRequirePlayermodels", GameRules.Category.PLAYER,
//					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<GameRules.BooleanRule> REVERT_TO_SMALL =
			GameRuleRegistry.register("cfaAlwaysRevertToWeakestForm", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

	public static void register() {

	}
}
