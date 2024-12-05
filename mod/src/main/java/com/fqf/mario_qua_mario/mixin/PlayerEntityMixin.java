package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.injections.MarioDataHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Inject(method = "travel", at = @At("HEAD"))
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		PlayerEntity meAsPlayer = (PlayerEntity) (Object) this;
		MarioQuaMario.LOGGER.info("Travel hook: \n{}\n{}\n{}", meAsPlayer, meAsPlayer.mqm$getMarioData(), meAsPlayer.mqm$getMarioData().getValue());
	}
}
