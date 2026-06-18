package com.fqf.mario_qua_mario.mixin.water_walking;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.injections.CfaDataHolder;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class MiniWaterWalkingMixin extends LivingEntityWaterWalkMixin implements CfaDataHolder {
	public MiniWaterWalkingMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Unique private static final double SQUARED_THRESHOLD = Powers.SPRINT_ON_WATER_THRESHOLD * Powers.SPRINT_ON_WATER_THRESHOLD;

	@Override @Unique
	protected boolean canWalkOnFluidHook(FluidState state) {
		return
				this.cfa$getCfaData().hasPower(Powers.SPRINT_ON_WATER)
				&& state.isIn(FluidTags.WATER)
				&& this.getVelocity().horizontalLengthSquared() > SQUARED_THRESHOLD;
	}
}
