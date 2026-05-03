package com.fqf.charapoweract.util;

import com.fqf.charapoweract.CharaPowerAct;
import com.fqf.charapoweract.bapping.BlockBappingUtil;
import com.fqf.charapoweract.cpadata.CPAServerPlayerData;
import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import com.fqf.charapoweract.packets.CPAPackets;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;

public class CPAEventListeners {
	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			CPAGamerules.useCharacterStats = server.getGameRules().getBoolean(CPAGamerules.USE_CHARACTER_STATS);
			CPAGamerules.restrictAdventureBapping = server.getGameRules().getBoolean(CPAGamerules.RESTRICT_ADVENTURE_BAPPING);
			CPAGamerules.adventurePlayersBreakBrittleBlocks = server.getGameRules().getBoolean(CPAGamerules.ADVENTURE_PLAYERS_BREAK_BRITTLE_BLOCKS);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			CPAServerPlayerData data = newPlayer.cpa$getCPAData();
			CPAServerPlayerData oldData = oldPlayer.cpa$getCPAData();
			if(oldData.isEnabled()) data.assignCharacter(oldData.getCharacterID());
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(((player, origin, destination) ->
				player.cpa$getCPAData().initialApply()));

		ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, amount) -> {
			if(!(livingEntity instanceof ServerPlayerEntity player)) return true;
			if(damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
			if( // try to detect if we're taking specifically poison damage. i wish so bad that this was its own damage type... ;-;
					damageSource.isOf(DamageTypes.MAGIC)
					&& player.hasStatusEffect(StatusEffects.POISON)
					&& amount == player.cpa$getCPAData().getCharacter().modifyIncomingDamage(player.cpa$getCPAData(), damageSource, 1)
			) {
				CharaPowerAct.LOGGER.info("Prevented player {} from either dying or reverting due to probable poison damage!", player.getName().getString());
				livingEntity.setHealth(1); // leave player on half a heart
				return false;
			}

			return player.cpa$getCPAData().executeReversion() != ICPAAuthoritativeData.ReversionResult.SUCCESS;
		});

//		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
//			if(trackedEntity instanceof ServerPlayerEntity player && player.cpa$getCPAData().isEnabled()) {
//				CPADataPackets.syncCPADataToPlayerS2C(player, player);
//			}
//		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CPAPackets.syncUseCharacterStatsS2C(handler.player, CPAGamerules.useCharacterStats);
			CPAPackets.syncRestrictAdventureBapsS2C(handler.player, CPAGamerules.restrictAdventureBapping, CPAGamerules.adventurePlayersBreakBrittleBlocks);
		});

		ServerTickEvents.START_WORLD_TICK.register(BlockBappingUtil::serverWorldTick);
	}
}
