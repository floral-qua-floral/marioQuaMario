package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.stomp_types.JumpStomp;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioContentEventListeners {
	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if(entity instanceof ServerPlayerEntity mario) {
				IMarioData data = mario.mqm$getIMarioData();
				if(data.isEnabled() && source.isDirect()) {
					Entity attacker = source.getAttacker();
					if(attacker != null) {
						if (JumpStomp.collidingFromTop(attacker, mario, null, false))
							return false;
						else
							MarioQuaMarioContent.LOGGER.info("Allowed Mario to take damage.\nMario Y: {}\nAttacker Top: {}",
									mario.getY(), attacker.getY() + attacker.getHeight());
					}
				}
			}
			return true;
		});
	}
}
