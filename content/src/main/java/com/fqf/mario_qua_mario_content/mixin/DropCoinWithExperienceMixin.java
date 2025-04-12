package com.fqf.mario_qua_mario_content.mixin;

import com.fqf.mario_qua_mario_content.item.ModItems;
import com.fqf.mario_qua_mario_content.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class DropCoinWithExperienceMixin extends Entity {
	@Shadow @Nullable protected PlayerEntity attackingPlayer;

	public DropCoinWithExperienceMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"))
	private void dropCoin(Entity attacker, CallbackInfo ci) {
		if(this.attackingPlayer != null && this.attackingPlayer.mqm$getIMarioData().hasPower(Powers.DROP_COINS))
			this.dropItem(ModItems.COIN);
	}
}
