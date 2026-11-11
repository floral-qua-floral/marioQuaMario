package com.fqf.mario_qua_mario.mixin.freezing;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class EncasedEntitiesNotPartOfGameMixin extends EntityEncasementMixin {
	@ModifyReturnValue(method = "isPartOfGame", at = @At("RETURN"))
	private boolean notPartOfGameIfEncased(boolean original) {
		return original && this.mqm$getEncasement() == null;
	}
}
