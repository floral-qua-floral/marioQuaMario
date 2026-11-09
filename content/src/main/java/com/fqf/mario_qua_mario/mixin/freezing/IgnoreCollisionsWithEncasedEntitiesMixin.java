package com.fqf.mario_qua_mario.mixin.freezing;

import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public class IgnoreCollisionsWithEncasedEntitiesMixin {
	@WrapWithCondition(method = "collideWithEntity", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V"
	))
	private boolean shouldAllowCollisionBehavior(Entity instance, PlayerEntity player) {
		return ((IceEncasementEntity.EncasementOwner) instance).mqm$getEncasement() == null;
	}
}
