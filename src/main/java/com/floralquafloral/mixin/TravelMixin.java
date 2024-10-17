package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.mariodata.MarioDataManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class TravelMixin {
	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		if(MarioDataManager.getMarioData(((PlayerEntity) (Object) this)) instanceof MarioClientData marioClientData
				&& marioClientData.useMarioPhysics() && marioClientData.travel(movementInput))
					ci.cancel();
	}
}
