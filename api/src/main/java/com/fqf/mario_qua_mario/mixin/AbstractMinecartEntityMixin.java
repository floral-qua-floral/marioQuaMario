package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.interfaces.StompResult;
import com.fqf.mario_qua_mario.interfaces.Stompable;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin implements Stompable {
	@Shadow public abstract AbstractMinecartEntity.Type getMinecartType();

	@Override
	public @NotNull StompResult mqm$stomp(IMarioAuthoritativeData marioData, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		if(this.getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE) {
			if(marioData.getMario().startRiding((Entity) (Object) this, false))
				return StompResult.MOUNT;
		}
		return Stompable.super.mqm$stomp(marioData, attemptMount, damageAmount, damageSource);
	}
}
