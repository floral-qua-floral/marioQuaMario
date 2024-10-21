package com.floralquafloral;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.util.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMario implements ModInitializer {
	public static final String MOD_ID = "qua_mario";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ModConfig CONFIG;
	static {
		LOGGER.info("Registering config");
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
	}

	public static final GameRules.Key<GameRules.BooleanRule> USE_CHARACTER_STATS =
			GameRuleRegistry.register("qua_mario:useCharacterStats", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true, (server, booleanRule) -> {
						for(ServerPlayerEntity player : PlayerLookup.all(server)) {
							ServerPlayNetworking.send(player, new MarioPackets.SyncUseCharacterStatsS2CPayload(true));
						}
					})
			);

	public static final GameRules.Key<GameRules.BooleanRule> REJECT_INVALID_ACTION_TRANSITIONS =
			GameRuleRegistry.register("qua_mario:rejectInvalidActionTransitions", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(true));

	public static final GameRules.Key<DoubleRule> INCOMING_DAMAGE_MULTIPLIER =
			GameRuleRegistry.register("qua_mario:marioIncomingDamageMultiplier", GameRules.Category.PLAYER,
					GameRuleFactory.createDoubleRule(2.5));
	// How to get value: marioWorld.getGameRules().get(MarioQuaMario.INCOMING_DAMAGE_MULTIPLIER).get()

	public static final GameRules.Key<DoubleRule> OUTGOING_DAMAGE_MULTIPLIER =
			GameRuleRegistry.register("qua_mario:marioOutgoingDamageMultiplier", GameRules.Category.PLAYER,
					GameRuleFactory.createDoubleRule(1.0));

	public static final GameRules.Key<GameRules.BooleanRule> REVERT_TO_SMALL =
			GameRuleRegistry.register("qua_mario:alwaysRevertToSmall", GameRules.Category.PLAYER,
					GameRuleFactory.createBooleanRule(false));

	@Override
	public void onInitialize() {
		LOGGER.info("MarioQuaMario.java loaded on environment type " + FabricLoader.getInstance().getEnvironmentType());
		MarioDataManager.registerEventListeners();

		RegistryManager.register();

		MarioPackets.registerCommon();

		MarioCommand.registerMarioCommand();

//		ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, damageAmount) -> {
//			if(livingEntity instanceof ServerPlayerEntity player) {
//				MarioData data = MarioDataManager.getMarioData(player);
//				if(data.isEnabled()) {
//					// Revert if possible
//					boolean canRevert = player.isOnGround();
//					if(canRevert) {
//						player.playSound(RegistryManager.POWER_UP_SFX);
//						data.setYVel(0.5);
//						player.setHealth(20.0F);
//						return false;
//					}
//				}
//			}
//			return true;
//		});
	}
}