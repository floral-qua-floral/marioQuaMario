package com.fqf.mario_qua_mario.mixin.freezing;

import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GhastEntity.class)
public abstract class GhastEntityFreezabilityMixin extends MobEntityFreezabilityMixin {
	@Override
	protected float getIceHorizontalInflation() {
		return 1;
	}

	@Override
	protected float getIceVerticalInflation() {
		return 1;
	}
}
