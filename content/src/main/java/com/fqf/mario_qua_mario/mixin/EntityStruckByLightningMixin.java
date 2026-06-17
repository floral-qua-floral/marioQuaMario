package com.fqf.mario_qua_mario.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(Entity.class)
public abstract class EntityStruckByLightningMixin {
	@Shadow public abstract World getWorld();

	@Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

	@Unique
	protected boolean doLightningMiniForm() {
		return false;
	}

	@WrapMethod(method = "onStruckByLightning")
	private void conditionallyDoMiniTransform(ServerWorld world, LightningEntity lightning, Operation<Void> original) {
		if(!this.doLightningMiniForm()) original.call(world, lightning);
	}
}
