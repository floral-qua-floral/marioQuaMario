package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioAnimationData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
	@Shadow @Final private ModelPart cloak;

	@Unique private ModelTransform storedCloakTransform;

	public PlayerEntityModelMixin(ModelPart root) {
		super(root);
		throw new AssertionError("Calling constructor on mixin?!");
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void resetBodyPartsToDefaultPose(T livingEntity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
		if(livingEntity instanceof AbstractClientPlayerEntity) {
			this.head.resetTransform();
			this.body.resetTransform();
			this.rightArm.resetTransform();
			this.leftArm.resetTransform();
			this.rightLeg.resetTransform();
			this.leftLeg.resetTransform();
			this.cloak.resetTransform();
		}
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
	private void animateMario(T livingEntity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
		if(livingEntity instanceof AbstractClientPlayerEntity mario && mario.mqm$getMarioData().isEnabled()) {
			mario.mqm$getAnimationData().setAngles(
					MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true), mario,
					this.head, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg,
					this.rightArmPose, this.leftArmPose
			);
			this.storedCloakTransform = this.cloak.getTransform();
		}
	}
}
