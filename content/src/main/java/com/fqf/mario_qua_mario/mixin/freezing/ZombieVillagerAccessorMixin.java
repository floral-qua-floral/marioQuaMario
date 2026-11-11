package com.fqf.mario_qua_mario.mixin.freezing;

import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZombieVillagerEntity.class)
public interface ZombieVillagerAccessorMixin {
	@Accessor
	NbtElement getGossipData();

	@Accessor
	TradeOfferList getOfferData();

	@Accessor
	int getXp();
}
