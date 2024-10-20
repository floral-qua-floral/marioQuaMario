package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.powerup.PowerUpDefinition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@WrapOperation(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud$HeartType;getTexture(ZZZ)Lnet/minecraft/util/Identifier;"))
	public Identifier usePowerUpHeart(InGameHud.HeartType instance, boolean hardcore, boolean half, boolean blinking, Operation<Identifier> original) {
		MarioClientData data = MarioClientData.getInstance();
		if(data == null) return original.call(instance, hardcore, half, blinking);

		if(instance == InGameHud.HeartType.CONTAINER) {
			PowerUpDefinition.PowerHeart heartContainer = data.getPowerUp().HEART_EMPTY;
			if(heartContainer == null) return original.call(instance, hardcore, half, blinking);
			else return heartContainer.getTexture(half, blinking);
		}
		else if(hardcore) {
			return data.getPowerUp().HEART_HARDCORE.getTexture(half, blinking);
		}
		else {
			return data.getPowerUp().HEART.getTexture(half, blinking);
		}
	}
}
