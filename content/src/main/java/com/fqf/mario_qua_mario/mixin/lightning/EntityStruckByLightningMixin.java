package com.fqf.mario_qua_mario.mixin.lightning;

import com.fqf.mario_qua_mario.util.LightningStrikableEntity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityStruckByLightningMixin implements LightningStrikableEntity {
	@Shadow public abstract World getWorld();

	@Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

	@WrapMethod(method = "onStruckByLightning")
	private void conditionallyDoMiniTransform(ServerWorld world, LightningEntity lightning, Operation<Void> original) {
		if(!this.mqm$resistLightningStrike()) original.call(world, lightning);
	}
}
