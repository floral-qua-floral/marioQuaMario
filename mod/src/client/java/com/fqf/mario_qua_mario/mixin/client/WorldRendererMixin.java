package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Shadow private @Nullable ClientWorld world;

	@Shadow @Final private BufferBuilderStorage bufferBuilders;

	@Shadow @Final private MinecraftClient client;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;long2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;", remap = false))
	private void renderCracksOnBrittleBlocks(
			RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
			LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci,
			@Local MatrixStack matrixStack
		) {
		if(this.world == null) return; // i don't know if it'll ever be null but i'm scared

		Profiler profiler = this.world.getProfiler();
		profiler.swap("mqm_brittleBlocks");

		Vec3d vec3d = camera.getPos();
		double d = vec3d.getX();
		double e = vec3d.getY();
		double g = vec3d.getZ();

		for(BlockPos pos : BlockBappingUtil.getCertain(BlockBappingUtil.BRITTLE_BLOCK_POSITIONS, this.world)) {
			if(BlockBappingUtil.HIDDEN_BLOCK_POSITIONS.getOrDefault(this.world, Set.of()).contains(pos))
				continue;

			double l = (double) pos.getX() - d;
			double m = (double) pos.getY() - e;
			double n = (double) pos.getZ() - g;

			if((!(l * l + m * m + n * n > 1024.0))) {
				matrixStack.push();
				matrixStack.translate((double)pos.getX() - d, (double)pos.getY() - e, (double)pos.getZ() - g);
				MatrixStack.Entry entry3 = matrixStack.peek();
				VertexConsumer vertexConsumer2 = new OverlayVertexConsumer(
						this.bufferBuilders.getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(9)), entry3, 1.0F
				);
				this.client.getBlockRenderManager().renderDamage(this.world.getBlockState(pos), pos, this.world, matrixStack, vertexConsumer2);
				matrixStack.pop();
			}
		}

		// now go on to actually render destroyProgress
		profiler.swap("destroyProgress");
	}
}
