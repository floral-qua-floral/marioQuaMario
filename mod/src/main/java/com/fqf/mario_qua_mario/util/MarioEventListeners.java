package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;

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

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((livingEntity, damageSource, amount) -> {
			if(!(livingEntity instanceof ServerPlayerEntity mario)) return true;

			MarioServerPlayerData data = mario.mqm$getMarioData();
			if(!data.isEnabled()) return true;

			if(data.getPowerUp().REVERSION_TARGET_ID == null) return true;

			data.revertTo(data.getPowerUp().REVERSION_TARGET_ID);
			return false;
		});
	}
}
