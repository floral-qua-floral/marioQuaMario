package com.fqf.mario_qua_mario.util;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.mario_qua_mario.collision_attacks.Stomp;
import com.fqf.mario_qua_mario.freezing.IceFlowerFreezable;
import com.fqf.mario_qua_mario.freezing.IceFlowerUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class MQMEventListeners {
	public static void register() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			if(damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.isOf(IceFlowerUtil.SHATTER_DAMAGE_TYPE))
				return true;

			IceFlowerFreezable freezable = (IceFlowerFreezable) entity;
			if(freezable.mqm$attemptFatalFreeze(damageSource)) {
				entity.setHealth(0.01F);
				return false;
			}

			return true;
		});

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

						if(
								Stomp.collidingFromTop(attacker, mario, marioY, new Vec3d(0, -1, 0), false)
								|| (attacker instanceof EnderDragonEntity && mario.getY() > attacker.getY() + attacker.getHeight() / 2)
						)
							return false;
					}
				}
			}

			return !((IceFlowerFreezable) entity).mqm$shatter(source, amount);
		});
	}

	private static boolean isPlayerEnabledInNBT(ServerPlayerEntity mario) {
		return new EntityDataObject(mario).getNbt().getCompound("mario_qua_mario.data").getBoolean("Enabled");
	}
}
