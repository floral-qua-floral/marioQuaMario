package com.fqf.mario_qua_mario.mixin;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.injections.CfaAuthoritativeDataHolder;
import com.fqf.mario_qua_mario.forms.Mini;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.Powers;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerStruckByLightningMixin extends EntityStruckByLightningMixin implements CfaAuthoritativeDataHolder {
	@Unique private long immuneToLightningUntil;

	@Override
	protected boolean doLightningMiniForm() {
		CfaAuthoritativeData data = this.cfa$getCfaAuthoritativeData();
		if(data.hasPower(Powers.LIGHTNING_SHRINKS)) {
			if(!data.getFormID().equals(Mini.ID)) {
				data.empowerTo(Mini.ID);
				this.immuneToLightningUntil = this.getWorld().getTime() + 50L;
				return true;
			}
			else return this.immuneToLightningUntil > this.getWorld().getTime();
		}
		return false;
	}
}
