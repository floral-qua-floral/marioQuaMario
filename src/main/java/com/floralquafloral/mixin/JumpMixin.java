package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioDataManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class JumpMixin {
	@WrapOperation(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;jump()V"))
	public void jump(PlayerEntity instance, Operation<Void> original) {
		MarioQuaMario.LOGGER.info("JUMP MIXIN:"
				+ "\nWorld: " + instance.getWorld()
				+ "\nisClient: " + instance.getWorld().isClient
				+ "\nisMainPlayer: " + instance.isMainPlayer()
				+ "\ngetMarioData: " + MarioDataManager.getMarioData(instance)
		);

		original.call(instance);
	}
}
