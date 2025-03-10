package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.injections.IMarioAuthoritativeDataHolder;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerStruckByLightningMixin extends PlayerEntity implements IMarioAuthoritativeDataHolder {
	public PlayerStruckByLightningMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
		throw new IllegalStateException();
	}

	@Override
	public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
		IMarioAuthoritativeData data = mqm$getIMarioAuthoritativeData();
		if(data.isEnabled()) {
			if(!data.getPowerUpID().toString().equals("mqm:mini"))
				data.empowerTo("mqm:mini");
		}
		else super.onStruckByLightning(world, lightning);
	}
}
