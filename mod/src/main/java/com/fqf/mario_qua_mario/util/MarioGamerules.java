package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.packets.MarioPackets;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

public class MarioGamerules {
	public static boolean useCharacterStats;

	public static final GameRules.Key<GameRules.BooleanRule> USE_CHARACTER_STATS =
			GameRuleRegistry.register(MarioQuaMario.makeID("useCharacterStats").toString(), GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						useCharacterStats = booleanRule.get();
						MarioPackets.syncUseCharacterStatsS2C(useCharacterStats);
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> REJECT_INVALID_ACTION_TRANSITIONS =
			GameRuleRegistry.register(MarioQuaMario.makeID("rejectInvalidActionTransitions").toString(), GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<DoubleRule> INCOMING_DAMAGE_MULTIPLIER =
			GameRuleRegistry.register(MarioQuaMario.makeID("marioIncomingDamageMultiplier").toString(), GameRules.Category.PLAYER,
					GameRuleFactory.createDoubleRule(2.5));
	// How to get value: marioWorld.getGameRules().get(MarioQuaMario.INCOMING_DAMAGE_MULTIPLIER).get()

	public static final GameRules.Key<DoubleRule> OUTGOING_DAMAGE_MULTIPLIER =
			GameRuleRegistry.register(MarioQuaMario.makeID("marioOutgoingDamageMultiplier").toString(), GameRules.Category.PLAYER,
					GameRuleFactory.createDoubleRule(1.0));

	public static final GameRules.Key<GameRules.BooleanRule> REVERT_TO_SMALL =
			GameRuleRegistry.register(MarioQuaMario.makeID("alwaysRevertToSmall").toString(), GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

	public static void register() {

	}
}
