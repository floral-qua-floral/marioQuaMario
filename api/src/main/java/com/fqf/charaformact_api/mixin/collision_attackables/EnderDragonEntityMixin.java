package com.fqf.charaformact_api.mixin.collision_attackables;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.interfaces.CollisionAttackable;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin implements CollisionAttackable {
	@Override
	public @NotNull CollisionAttackResult cfa$processCollisionAttack(CfaAuthoritativeData data, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		return CollisionAttackResult.FAIL; // This behaviour is handled by EnderDragonPart instead. The Dragon's own hitbox is much too large.
	}
}
