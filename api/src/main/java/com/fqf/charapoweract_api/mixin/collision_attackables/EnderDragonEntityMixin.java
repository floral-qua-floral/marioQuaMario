package com.fqf.charapoweract_api.mixin.collision_attackables;

import com.fqf.charapoweract_api.interfaces.CollisionAttackResult;
import com.fqf.charapoweract_api.interfaces.CollisionAttackable;
import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin implements CollisionAttackable {
	@Override
	public @NotNull CollisionAttackResult cpa$processCollisionAttack(ICPAAuthoritativeData data, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		return CollisionAttackResult.FAIL; // EnderDragonPart should be stomped instead!
	}
}
