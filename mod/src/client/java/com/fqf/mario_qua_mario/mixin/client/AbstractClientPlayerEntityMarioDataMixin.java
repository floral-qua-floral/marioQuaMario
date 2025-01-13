package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.mariodata.MarioAnimationData;
import com.fqf.mario_qua_mario.mariodata.injections.MarioAbstractClientDataHolder;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMarioDataMixin implements MarioAbstractClientDataHolder {
	@Unique
	private final MarioAnimationData ANIMATION_DATA = new MarioAnimationData();

	@Override public @NotNull MarioAnimationData mqm$getAnimationData() {
		return this.ANIMATION_DATA;
	}
}
