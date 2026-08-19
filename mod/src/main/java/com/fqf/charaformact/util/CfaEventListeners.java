package com.fqf.charaformact.util;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact.packets.CfaPackets;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class CfaEventListeners {
	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			CfaGamerules.useCharacterStats = server.getGameRules().getBoolean(CfaGamerules.USE_CHARACTER_STATS);
			CfaGamerules.restrictAdventureBapping = server.getGameRules().getBoolean(CfaGamerules.RESTRICT_ADVENTURE_BAPPING);
			CfaGamerules.adventurePlayersBreakBrittleBlocks = server.getGameRules().getBoolean(CfaGamerules.ADVENTURE_PLAYERS_BREAK_BRITTLE_BLOCKS);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			float healthFullProportion = newPlayer.getHealth() / newPlayer.getMaxHealth();
			CfaServerPlayerData data = newPlayer.cfa$getCfaData();
			CfaServerPlayerData oldData = oldPlayer.cfa$getCfaData();
			if(oldData.isEnabled()) {
				data.assignCharacter(oldData.getCharacterID());

				// In vanilla, players will of course always respawn with maximum health.
				// But I've seen some mods that make it so the player can spawn missing some health or hunger. So here
				// we try to maintain the ratio of filled hearts to empty hearts from when the player initially
				// respawned, before we gave them their extra health bars. Hopefully this will help?
				if(data.getHealthBarCount() > 1)
					newPlayer.setHealth(healthFullProportion * newPlayer.getMaxHealth());
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CfaPackets.syncGamerulesS2C(handler.player);
			handler.player.cfa$getCfaData().initialApply();
			handler.player.setHealth(handler.player.cfa$getCfaData().initialHealth);
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

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if(!(entity instanceof ServerPlayerEntity player) || entity.isDead()) return;
			CfaServerPlayerData data = player.cfa$getCfaData();
			if(data.getHealthBarCount() <= 1) return;

			ParsedForm revertTo = data.getForm();
			while(revertTo != null && entity.getHealth() <= (revertTo.getHealthBarCount() - 1) * data.getSingleHealthBarSize()) {
				revertTo = revertTo.getReversionTarget();
			}
			if(revertTo != null && revertTo != data.getForm()) {
				if(data.revertTo(revertTo) == CfaAuthoritativeData.FormChangeOperationResult.NO_VALID_APPEARANCE)
					CharaFormAct.LOGGER.warn("Trying to revert from {} to {}, but character {} has no Appearance for {}!",
							data.getFormID(), revertTo.ID, data.getCharacterID(), revertTo.ID);
			}
		});

		ServerTickEvents.START_WORLD_TICK.register(BlockBappingUtil::serverWorldTick);


	}
}
