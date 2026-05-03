package com.fqf.charapoweract.util;

import com.fqf.charapoweract.MarioQuaMario;
import com.fqf.charapoweract.bapping.BlockBappingUtil;
import com.fqf.charapoweract.cpadata.CPAServerPlayerData;
import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import com.fqf.charapoweract.packets.MarioPackets;
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

public class MarioEventListeners {
	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MarioGamerules.useCharacterStats = server.getGameRules().getBoolean(MarioGamerules.USE_CHARACTER_STATS);
			MarioGamerules.restrictAdventureBapping = server.getGameRules().getBoolean(MarioGamerules.RESTRICT_ADVENTURE_BAPPING);
			MarioGamerules.adventurePlayersBreakBrittleBlocks = server.getGameRules().getBoolean(MarioGamerules.ADVENTURE_PLAYERS_BREAK_BRITTLE_BLOCKS);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			CPAServerPlayerData data = newPlayer.cpa$getCPAData();
			CPAServerPlayerData oldData = oldPlayer.cpa$getCPAData();
			if(oldData.isEnabled()) data.assignCharacter(oldData.getCharacterID());
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(((player, origin, destination) ->
				player.cpa$getCPAData().initialApply()));

		ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, amount) -> {
			if(!(livingEntity instanceof ServerPlayerEntity mario)) return true;
			if(damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
			if( // try to detect if we're taking specifically poison damage. i wish so bad that this was its own damage type... ;-;
					damageSource.isOf(DamageTypes.MAGIC)
					&& mario.hasStatusEffect(StatusEffects.POISON)
					&& amount == mario.cpa$getCPAData().getCharacter().modifyIncomingDamage(mario.cpa$getCPAData(), damageSource, 1)
			) {
				MarioQuaMario.LOGGER.info("Prevented player {} from either dying or reverting due to probable poison damage!", mario.getName().getString());
				livingEntity.setHealth(1); // leave player on half a heart
				return false;
			}

			return mario.cpa$getCPAData().executeReversion() != ICPAAuthoritativeData.ReversionResult.SUCCESS;
		});

//		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
//			if(trackedEntity instanceof ServerPlayerEntity mario && mario.cpa$getCPAData().isEnabled()) {
//				MarioDataPackets.syncMarioDataToPlayerS2C(mario, player);
//			}
//		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			MarioPackets.syncUseCharacterStatsS2C(handler.player, MarioGamerules.useCharacterStats);
			MarioPackets.syncRestrictAdventureBapsS2C(handler.player, MarioGamerules.restrictAdventureBapping, MarioGamerules.adventurePlayersBreakBrittleBlocks);
		});

		ServerTickEvents.START_WORLD_TICK.register(BlockBappingUtil::serverWorldTick);
	}
}
