package com.fqf.mario_qua_mario_content.mixin.client;

import com.fqf.mario_qua_mario_content.MarioQuaMarioContentClient;
import com.fqf.mario_qua_mario_content.item.ModItems;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntityRenderer.class)
public class ChangeDroppedCoinModelMixin {
	@Shadow @Final private ItemRenderer itemRenderer;

	@WrapMethod(method = "renderStack(Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/BakedModel;ZLnet/minecraft/util/math/random/Random;)V")
	private static void changeCoinModel(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, BakedModel model, boolean depth, Random random, Operation<Void> original) {
		if(stack.isOf(ModItems.COIN))
			model = itemRenderer.getModels().getModelManager().getModel(MarioQuaMarioContentClient.COIN_GROUND_MODEL_ID);

		original.call(itemRenderer, matrices, vertexConsumers, light, stack, model, depth, random);
	}
}
