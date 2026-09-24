package com.fqf.mario_qua_mario.mixin.freezing;

import net.minecraft.entity.passive.StriderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StriderEntity.class)
public abstract class StriderEntityFreezabilityMixin extends MobEntityFreezabilityMixin {
	@Shadow public abstract void setCold(boolean cold);

	@Override
	protected void onEncased() {
		super.onEncased();
		this.setCold(true);
	}
}
