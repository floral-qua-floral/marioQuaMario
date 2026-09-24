package com.fqf.mario_qua_mario.mixin.freezing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityFreezabilityMixin extends LivingEntityFreezabilityMixin {
	@Override
	public boolean mqm$canBeEncased() {
		return false;
	}

	@Override
	public boolean mqm$tickEncased() {
		return true;
	}

	@Override
	public boolean mqm$encase() {
		return false;
	}

	@Override
	public boolean mqm$iceFlowerHit(@NotNull Entity source, @NotNull Entity attacker) {
		// Powder Snow-style freezing since players are WAY too complicated for me to feel comfortable suppressing their
		// ticking or movement, and it would be super overpowered anyways
		if(!this.canFreeze()) return false;
		this.setFrozenTicks(Math.max(this.getMinFreezeDamageTicks(), this.getFrozenTicks() + 50));
		return true;
	}
}
