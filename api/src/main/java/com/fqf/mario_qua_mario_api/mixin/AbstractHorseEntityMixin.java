package com.fqf.mario_qua_mario_api.mixin;

import com.fqf.mario_qua_mario_api.interfaces.StompResult;
import com.fqf.mario_qua_mario_api.interfaces.Stompable;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityMixin implements Stompable {
	@Override
	public @NotNull StompResult mqm$stomp(IMarioAuthoritativeData marioData, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		if(marioData.getMario().startRiding((Entity) (Object) this, false))
			return StompResult.MOUNT;
		else
			return Stompable.super.mqm$stomp(marioData, attemptMount, damageAmount, damageSource);
	}
}
