package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.bapping.WorldBapsInfo;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.models.ParsedCharacterFormModel;
import com.fqf.charaformact.util.CfaGamerules;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final MinecraftClient client;

	@Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
	private void skipRenderHandForNow(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
		ci.cancel();
	}

	@WrapMethod(method = "bobView")
	private void scaleMovementSpeedAroundBobbing(MatrixStack matrices, float tickDelta, Operation<Void> original) {
		if(this.client.getCameraEntity() instanceof AbstractClientPlayerEntity player) {
			ParsedCharacterFormModel model = player.cfa$getModelData().getModel();
			if(model != null) {
				float realSpeed = player.horizontalSpeed;
				float realPreviousSpeed = player.prevHorizontalSpeed;
				player.horizontalSpeed *= model.LIMB_SWING_MULTIPLIER;
				player.prevHorizontalSpeed *= model.LIMB_SWING_MULTIPLIER;
				original.call(matrices, tickDelta);
				player.horizontalSpeed = realSpeed;
				player.prevHorizontalSpeed = realPreviousSpeed;
				return;
			}
		}
		original.call(matrices, tickDelta);
	}

	@WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
	private void scaleBobbing(MatrixStack instance, float x, float y, float z, Operation<Void> original) {
		CfaPlayerData data = ((PlayerEntity) Objects.requireNonNull(this.client.getCameraEntity())).cfa$getCfaData();
		float horizontalScale = data.getHorizontalScale();
		original.call(instance, x * horizontalScale, y * data.getEyeHeightScale(), z * horizontalScale);
	}

	@Inject(method = "shouldRenderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/pattern/CachedBlockPosition;<init>(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Z)V"), cancellable = true)
	private void optionallyRenderOutlineOnBrittleBlocks(CallbackInfoReturnable<Boolean> cir) {
		assert this.client.world != null && this.client.crosshairTarget != null;
		if(CfaGamerules.adventurePlayersBreakBrittleBlocks) {
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(this.client.world);
			if(worldBaps != null) {
				if(worldBaps.BRITTLE.contains(((BlockHitResult) this.client.crosshairTarget).getBlockPos()))
					cir.setReturnValue(true);
			}
		}
	}
}
