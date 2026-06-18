package com.fqf.mario_qua_mario.mixin.lightning;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.injections.CfaAuthoritativeDataHolder;
import com.fqf.mario_qua_mario.forms.Mini;
import com.fqf.mario_qua_mario.util.LightningStrikableEntity;
import com.fqf.mario_qua_mario.util.LightningStrikableInventory;
import com.fqf.mario_qua_mario.util.Powers;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerStruckByLightningMixin extends PlayerEntity implements LightningStrikableEntity, CfaAuthoritativeDataHolder {
	@Unique private long immuneToLightningUntil;

	public PlayerStruckByLightningMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Override
	public boolean mqm$resistLightningStrike() {
		((LightningStrikableInventory) this.getInventory()).mqm$strike();
		CfaAuthoritativeData data = this.cfa$getCfaAuthoritativeData();
		if(data.hasPower(Powers.LIGHTNING_SHRINKS)) {
			if(!data.getFormID().equals(Mini.ID)) {
				data.empowerTo(Mini.ID);
				// 6 seconds of Fire Resistance in addition to no damage from the lightning bolt because Mini Mario is
				// RIDICULOUSLY fragile ^^;
				this.immuneToLightningUntil = this.getWorld().getTime() + 50L;
				this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 120));
				return true;
			}
			else return this.immuneToLightningUntil > this.getWorld().getTime();
		}
		return false;
	}
}
