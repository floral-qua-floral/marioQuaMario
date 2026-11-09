package com.fqf.mario_qua_mario.mixin.freezing;

import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface DeathSoundAccessor {
	@Invoker
	SoundEvent invokeGetDeathSound();

	@Invoker
	void invokeUpdatePostDeath();
}
