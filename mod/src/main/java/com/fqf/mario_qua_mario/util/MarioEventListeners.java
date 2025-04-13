package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.packets.MarioPackets;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioEventListeners {
	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
				MarioGamerules.useCharacterStats = server.getGameRules().getBoolean(MarioGamerules.USE_CHARACTER_STATS));

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			MarioServerPlayerData data = newPlayer.mqm$getMarioData();
			MarioServerPlayerData oldData = oldPlayer.mqm$getMarioData();
			if(oldData.isEnabled()) data.assignCharacter(oldData.getCharacterID());
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(((player, origin, destination) ->
				player.mqm$getMarioData().initialApply()));

		ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, amount) -> {
			if(!(livingEntity instanceof ServerPlayerEntity mario)) return true;
			if(damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
//			if(damageSource.isOf(DamageTypes.MAGIC) && amount == mario.mqm$getMarioData().getCharacter().modifyIncomingDamage(mario.mqm$getMarioData(), damageSource, 1))

			return mario.mqm$getMarioData().executeReversion() != IMarioAuthoritativeData.ReversionResult.SUCCESS;
		});

//		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
//			if(trackedEntity instanceof ServerPlayerEntity mario && mario.mqm$getMarioData().isEnabled()) {
//				MarioDataPackets.syncMarioDataToPlayerS2C(mario, player);
//			}
//		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			MarioPackets.syncUseCharacterStatsS2C(handler.player, MarioGamerules.useCharacterStats);
		});
	}
}
