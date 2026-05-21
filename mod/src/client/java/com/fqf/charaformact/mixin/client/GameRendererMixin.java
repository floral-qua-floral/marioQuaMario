package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.bapping.WorldBapsInfo;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.util.CfaGamerules;
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
import org.spongepowered.asm.mixin.Unique;
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

	@WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sin(F)F"))
	private float scaleViewBobbingTimescaleOnlySin(float value, Operation<Float> original) {
		return this.scaleTrigonometryMethod(value, original);
	}

	@WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;cos(F)F"))
	private float scaleViewBobbingTimescaleOnlyCos(float value, Operation<Float> original) {
		return this.scaleTrigonometryMethod(value, original);
	}

	@Unique
	private float scaleTrigonometryMethod(float value, Operation<Float> original) {
		// We know for sure going into this that the camera entity is a player! No need to check!
		assert this.client.getCameraEntity() != null;
		ParsedClientAppearance appearance = ((AbstractClientPlayerEntity) this.client.getCameraEntity())
				.cfa$getAppearanceData().getAppearance(false);
		if(appearance != null) {
			return original.call(value * appearance.VIEW_BOB_MULTIPLIER);
		}
		return original.call(value);
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
