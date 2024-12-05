package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.injections.MarioDataHolder;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMarioDataMixin implements MarioDataHolder {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
		MarioQuaMario.LOGGER.info("Initialized a PlayerEntity with MarioData:\n{}\n{}\n{}", (PlayerEntity) (Object) this, this, this.mqm$getMarioData());
	}
}
