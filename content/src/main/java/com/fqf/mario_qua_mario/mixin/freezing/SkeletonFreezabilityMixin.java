package com.fqf.mario_qua_mario.mixin.freezing;

import net.minecraft.entity.mob.SkeletonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SkeletonEntity.class)
public abstract class SkeletonFreezabilityMixin extends MobEntityFreezabilityMixin {
	@Shadow protected abstract void convertToStray();

	@Override
	public boolean mqm$thaw() {
		if(super.mqm$thaw()) {
			if(!this.isDead() && this.getHealth() > 0.01F) this.convertToStray();
			return true;
		}
		return false;
	}
}
