package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class MarioEventListeners {
	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
				MarioGamerules.useCharacterStats = server.getGameRules().getBoolean(MarioGamerules.USE_CHARACTER_STATS));

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			MarioServerPlayerData data = newPlayer.mqm$getMarioData();

			data.setEnabled(oldPlayer.mqm$getMarioData().isEnabled());
			data.assignCharacter(oldPlayer.mqm$getMarioData().getCharacterID());
			data.assignPowerUp(data.getCharacter().INITIAL_POWER_UP.ID);
			data.assignAction(data.getCharacter().INITIAL_ACTION.ID);
		});

		ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, amount) -> {
			if(damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;

			MarioQuaMario.LOGGER.info("Allow Death event on {}", livingEntity);
			if(!(livingEntity instanceof ServerPlayerEntity mario)) return true;

			return mario.mqm$getMarioData().executeReversion() != MarioServerPlayerData.ReversionResult.SUCCESS;
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
//			handler.player.mqm$getMarioData().initialApply();
//			handler.player.mqm$getMarioData().updatePlayerModel();
//			handler.player.mqm$getMarioData().syncToClient(handler.player);
		});
	}
}
