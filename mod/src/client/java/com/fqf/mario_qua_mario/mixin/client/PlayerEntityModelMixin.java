package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
	public PlayerEntityModelMixin(ModelPart root) {
		super(root);
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
	private void setAnglesHook(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		PlayerEntity mario = (PlayerEntity) livingEntity;
		MarioPlayerData data = mario.mqm$getMarioData();

		if(!data.isEnabled()) return;

		this.rightArm.setAngles(0, 0, 0);
		this.rightArm.pivotY += 3;
	}
}
