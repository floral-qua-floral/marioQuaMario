package com.fqf.mario_qua_mario.mixin.freezing.outdated;

import net.minecraft.entity.mob.GhastEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GhastEntity.class)
public abstract class GhastEntityFreezabilityMixin extends MobEntityFreezabilityMixin {
	@Override
	protected float getIceHorizontalInflation() {
		return 2;
	}

	@Override
	protected float getIceVerticalInflation() {
		return 0.5F;
	}
}
