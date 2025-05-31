package com.fqf.mario_qua_mario_api.mixin;

import com.fqf.mario_qua_mario_api.MarioQuaMarioAPI;
import com.fqf.mario_qua_mario_api.interfaces.StompResult;
import com.fqf.mario_qua_mario_api.interfaces.Stompable;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderDragonPart.class)
public class EnderDragonPartMixin implements Stompable {
	@Override
	public @NotNull StompResult mqm$stomp(IMarioAuthoritativeData marioData, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		MarioQuaMarioAPI.LOGGER.info("Attempting stomp on EnderDragonPart...!");
		return Stompable.super.mqm$stomp(marioData, attemptMount, damageAmount, damageSource);
	}
}
