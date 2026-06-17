package com.fqf.charaformact.util;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact.packets.CfaPackets;
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

public class CfaEventListeners {
	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			CfaGamerules.useCharacterStats = server.getGameRules().getBoolean(CfaGamerules.USE_CHARACTER_STATS);
			CfaGamerules.restrictAdventureBapping = server.getGameRules().getBoolean(CfaGamerules.RESTRICT_ADVENTURE_BAPPING);
			CfaGamerules.adventurePlayersBreakBrittleBlocks = server.getGameRules().getBoolean(CfaGamerules.ADVENTURE_PLAYERS_BREAK_BRITTLE_BLOCKS);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			CfaServerPlayerData data = newPlayer.cfa$getCfaData();
			CfaServerPlayerData oldData = oldPlayer.cfa$getCfaData();
			if(oldData.isEnabled()) data.assignCharacter(oldData.getCharacterID());
		});



		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(((player, origin, destination) -> {
			CfaServerPlayerData data = player.cfa$getCfaData();
//			if(data.isEnabled()) {
//				data.getCharacter().onExit(data);
//				data.getForm().onExit(data);
//				data.getAction().onExit(data);
//			}
			data.initialApply();
		}));

		ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, amount) -> {
			if(!(livingEntity instanceof ServerPlayerEntity player)) return true;
			if(damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
			if( // try to detect if we're taking specifically poison damage. i wish so bad that this was its own damage type... ;-;
					damageSource.isOf(DamageTypes.MAGIC)
					&& player.hasStatusEffect(StatusEffects.POISON)
					&& amount == player.cfa$getCfaData().getCharacter().modifyIncomingDamage(player.cfa$getCfaData(), damageSource, 1)
			) {
				CharaFormAct.LOGGER.info("Prevented player {} from either dying or reverting due to probable poison damage!", player.getName().getString());
				livingEntity.setHealth(1); // leave player on half a heart
				return false;
			}

			return player.cfa$getCfaData().executeReversion() != CfaAuthoritativeData.ReversionResult.SUCCESS;
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CfaPackets.syncGamerulesS2C(handler.player);
			handler.player.cfa$getCfaData().initialApply();
		});

		ServerTickEvents.START_WORLD_TICK.register(BlockBappingUtil::serverWorldTick);


	}
}
