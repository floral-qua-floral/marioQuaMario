package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.cfadata.injections.AdvCfaAbstractClientDataHolder;
import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity implements AdvCfaAbstractClientDataHolder {
	public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Override
	protected void updateLimbs(float posDelta) {
		ParsedClientAppearance model = this.cfa$getModelData().getModel();
		super.updateLimbs((model == null ? 1 : model.LIMB_SWING_MULTIPLIER) * posDelta);
	}
}
