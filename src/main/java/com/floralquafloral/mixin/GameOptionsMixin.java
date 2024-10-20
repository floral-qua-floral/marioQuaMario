package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMarioClient;
import com.floralquafloral.mariodata.client.MarioClientData;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
	@Inject(method = "getBobView", at = @At("HEAD"), cancellable = true)
	public void preventViewBobbing(CallbackInfoReturnable<SimpleOption<Boolean>> cir) {
		MarioClientData data = MarioClientData.getInstance();
		if(data != null && !data.getAction().SLIDING_STATUS.doViewBobbing())
			cir.setReturnValue(MarioQuaMarioClient.ALWAYS_FALSE);
	}
}
