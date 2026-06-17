package com.fqf.mario_qua_mario.util;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.characters.Mario;
import com.fqf.mario_qua_mario.collision_attacks.Stomp;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class MQMEventListeners {
	public static void register() {


		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if(entity instanceof ServerPlayerEntity mario) {
				CfaData data = mario.cfa$getCfaData();
				if(data.isEnabled() && data.hasPower(Powers.STOMP_GUARD) && source.isDirect() && !source.isIn(MQMTags.BYPASSES_STOMP_GUARD)) {
					Entity attacker = source.getAttacker();
					if(attacker != null) {
						double marioY;
						if(data.retrieveStateData(MarioVars.class).stompGuardRemainingTicks > 0)
							marioY = Math.max(mario.getY(), data.retrieveStateData(MarioVars.class).stompGuardMinHeight);
						else
							marioY = mario.getY();

						//noinspection RedundantIfStatement
						if(Stomp.collidingFromTop(attacker, mario, marioY, new Vec3d(0, -1, 0), false)
								|| (attacker instanceof EnderDragonEntity && mario.getY() > attacker.getY() + attacker.getHeight() / 2)) {
							return false;
						}
					}
				}
			}
			return true;
		});
	}

	private static boolean isPlayerEnabledInNBT(ServerPlayerEntity mario) {
		return new EntityDataObject(mario).getNbt().getCompound("mario_qua_mario.data").getBoolean("Enabled");
	}
}
