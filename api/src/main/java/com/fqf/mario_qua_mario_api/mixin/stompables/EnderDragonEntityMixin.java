package com.fqf.mario_qua_mario_api.mixin.stompables;

import com.fqf.mario_qua_mario_api.interfaces.StompResult;
import com.fqf.mario_qua_mario_api.interfaces.CollisionAttackable;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin implements CollisionAttackable {
	@Override
	public @NotNull StompResult mqm$processCollisionAttack(IMarioAuthoritativeData marioData, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		return StompResult.FAIL; // EnderDragonPart should be stomped instead!
	}
}
