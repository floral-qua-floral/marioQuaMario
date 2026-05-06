package com.fqf.mario_qua_mario.mixin;

import com.fqf.charaformact_api.cfadata.injections.CfaAuthoritativeDataHolder;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerStruckByLightningMixin extends PlayerEntity implements CfaAuthoritativeDataHolder {
	public PlayerStruckByLightningMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
		throw new IllegalStateException();
	}

	// TODO: Uncomment once Mini form is available
//	@Override
//	public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
//		CfaAuthoritativeData data = mqm$getIMarioAuthoritativeData();
//		if(data.isEnabled()) {
//			if(!data.getFormID().toString().equals("mqm:mini"))
//				data.empowerTo("mqm:mini");
//		}
//		else super.onStruckByLightning(world, lightning);
//	}
}
