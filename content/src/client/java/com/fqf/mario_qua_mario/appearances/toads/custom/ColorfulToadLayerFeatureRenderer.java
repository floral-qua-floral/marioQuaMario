package com.fqf.mario_qua_mario.appearances.toads.custom;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.OptionalInt;

import static com.fqf.mario_qua_mario.util.CustomToadUtil.*;

public abstract class ColorfulToadLayerFeatureRenderer<TrackedType> extends FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel> {
	private final Identifier TEXTURE;
	protected final TrackedData<TrackedType> TRACKED_DATA;

	public ColorfulToadLayerFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, String folder, String layer, TrackedData<TrackedType> trackedData) {
		super(context);

		this.TEXTURE = Identifier.of("mario_qua_mario", "textures/entity/player/appearance/customizable_toad/features/" + folder + "/" + layer + ".png");
		this.TRACKED_DATA = trackedData;
	}

	public enum SpotsMode {
		DEFAULT,
		INVERTED,
		HARDCODED
	}

	public static List<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> makeFeatureRenderers(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, String folder, SpotsMode spots) {
		ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder = ImmutableList.builderWithExpectedSize(spots == SpotsMode.HARDCODED ? 4 : 5);

		builder.add(new TracksInteger(context, folder, "skin", SKIN_COLOR));

		switch(spots) {
			case DEFAULT:
				builder.add(
						new TracksInteger(context, folder, "cap", CAP_COLOR),
						new TracksInteger(context, folder, "spots", SPOTS_COLOR)
				);
				break;
			case INVERTED:
				builder.add(new TracksInteger(context, folder, "spots", CAP_COLOR));
			case HARDCODED: // Switch-case fallthrough my beloved!!!!!!!!!!!
				builder.add(new TracksInteger(context, folder, "cap", SPOTS_COLOR));
		}

		builder.add(new TracksInteger(context, folder, "vest", VEST_COLOR));
		builder.add(new TracksOptionalInteger(context, folder, "shirt", SHIRT_COLOR));

		return builder.build();
	}

	@Override
	public final void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity toad, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		this.render(matrices, vertexConsumers, light, toad, ((CustomToadEntity) toad).mqm$getToadData(this.TRACKED_DATA));
	}

	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity toad, TrackedType trackedValue) {
		if(!toad.isInvisible()) {
			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.TEXTURE));
			this.getContextModel().render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, this.getColor(trackedValue));
		}
	}

	protected abstract int getColor(TrackedType trackedValue);

	private static class TracksInteger extends ColorfulToadLayerFeatureRenderer<Integer> {
		public TracksInteger(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, String folder, String layer, TrackedData<Integer> trackedData) {
			super(context, folder, layer, trackedData);
		}

		@Override
		protected int getColor(Integer trackedValue) {
			return trackedValue;
		}
	}

	private static class TracksOptionalInteger extends ColorfulToadLayerFeatureRenderer<OptionalInt> {
		public TracksOptionalInteger(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, String folder, String layer, TrackedData<OptionalInt> trackedData) {
			super(context, folder, layer, trackedData);
		}

		@Override
		public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity toad, OptionalInt trackedValue) {
			if(trackedValue.isPresent()) super.render(matrices, vertexConsumers, light, toad, trackedValue);
		}

		@Override
		protected int getColor(OptionalInt trackedValue) {
			return trackedValue.orElseThrow();
		}
	}
}
