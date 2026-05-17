package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.bapping.BlockBappingClientUtil;
import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.models.CfaPlayerModelHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Shadow private Map<EntityType<?>, EntityRenderer<?>> renderers;

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getPositionOffset(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;"))
	private <E extends Entity> Vec3d modifyEntityOffset(EntityRenderer<? super E> instance, E entity, float tickDelta, Operation<Vec3d> original) {
		return original.call(instance, entity, tickDelta).add(BlockBappingClientUtil.calculateDubiousOffsetUnder(entity, tickDelta));
	}

	@Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
	private <T extends Entity> void getAlternateRendererForCharacters(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
		if(entity instanceof AbstractClientPlayerEntity player) {
			EntityRenderer<AbstractClientPlayerEntity> storedRenderer = ((CfaClientDataImpl) player.cfa$getCfaClientData()).getStoredRenderer();
			if(storedRenderer != null) {
				//noinspection unchecked
				cir.setReturnValue((EntityRenderer<? super T>) storedRenderer);
			}
		}
	}
}
