package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract.bapping.BlockBappingUtil;
import com.fqf.charapoweract.bapping.WorldBapsInfo;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract.util.MarioGamerules;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final MinecraftClient client;

	@WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
	private void scaleBobbing(MatrixStack instance, float x, float y, float z, Operation<Void> original) {
		CPAPlayerData data = ((PlayerEntity) Objects.requireNonNull(this.client.getCameraEntity())).cpa$getCPAData();
		float horizontalScale = data.getHorizontalScale();
		original.call(instance, x * horizontalScale, y * data.getEyeHeightScale(), z * horizontalScale);
	}

	@Inject(method = "shouldRenderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/pattern/CachedBlockPosition;<init>(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Z)V"), cancellable = true)
	private void optionallyRenderOutlineOnBrittleBlocks(CallbackInfoReturnable<Boolean> cir) {
		assert this.client.world != null && this.client.crosshairTarget != null;
		if(MarioGamerules.adventurePlayersBreakBrittleBlocks) {
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(this.client.world);
			if(worldBaps != null) {
				if(worldBaps.BRITTLE.contains(((BlockHitResult) this.client.crosshairTarget).getBlockPos()))
					cir.setReturnValue(true);
			}
		}
	}
}
