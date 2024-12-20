package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
			if(!(livingEntity instanceof ServerPlayerEntity mario)) return true;

			MarioServerPlayerData data = mario.mqm$getMarioData();
			if(!data.isEnabled()) return true;

			Identifier reversionTarget = data.getPowerUp().REVERSION_TARGET_ID;
			if(reversionTarget == null) return true;

			if(livingEntity.getWorld().getGameRules().getBoolean(MarioGamerules.REVERT_TO_SMALL)) {
				MarioQuaMario.LOGGER.info("Forcing maximum possible reversion!");
				while(Objects.requireNonNull(RegistryManager.POWER_UPS.get(reversionTarget)).REVERSION_TARGET_ID != null) {
					reversionTarget = Objects.requireNonNull(RegistryManager.POWER_UPS.get(reversionTarget)).REVERSION_TARGET_ID;
				}
			}
			data.revertTo(reversionTarget);
			mario.setHealth(mario.getMaxHealth());
			return false;
		});
	}
}
