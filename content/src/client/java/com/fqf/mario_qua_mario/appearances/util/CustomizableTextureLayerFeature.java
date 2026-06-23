package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.customization.CustomizablePlayerEntity;
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

import static com.fqf.mario_qua_mario.customization.CharacterCustomizationUtil.*;

public abstract class CustomizableTextureLayerFeature<TrackedType> extends FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel> {
	private final Identifier TEXTURE;
	protected final TrackedData<TrackedType> TRACKED_DATA;

	public CustomizableTextureLayerFeature(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, Identifier texture, String folder, String layer, TrackedData<TrackedType> trackedData) {
		super(context);

		this.TEXTURE = Identifier.of(texture.getNamespace(), texture.getPath().substring(0, texture.getPath().lastIndexOf('/')) + "/features/" + folder + "/" + layer + ".png");
		MarioQuaMario.LOGGER.info("Made a customizable texture feature with path {}", this.TEXTURE);
		this.TRACKED_DATA = trackedData;
	}

	@Override
	public final void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity toad, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		this.render(matrices, vertexConsumers, light, toad, ((CustomizablePlayerEntity) toad).mqm$getCustomizationData(this.TRACKED_DATA));
	}

	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity toad, TrackedType trackedValue) {
		if(!toad.isInvisible()) {
			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.TEXTURE));
			this.getContextModel().render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, this.getColor(trackedValue));
		}
	}

	protected abstract int getColor(TrackedType trackedValue);

	public enum SpotsMode {
		DEFAULT,
		INVERTED,
		HARDCODED
	}

	public static FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel> makeOptionalSkinFeature(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, Identifier texture, String folder) {
		return new TracksIntegerAndBoolean(context, texture, folder, "skin", SKIN_COLOR, ALWAYS_USE_SKIN_COLOR);
	}

	public static List<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> makeCustomToadFeatures(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, Identifier texture, String folder, SpotsMode spots) {
		ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder = ImmutableList.builderWithExpectedSize(spots == SpotsMode.HARDCODED ? 3 : 5);

		builder.add(new TracksInteger(context, texture, folder, "skin", SKIN_COLOR));

		switch(spots) {
			case DEFAULT:
				builder.add(
						new TracksInteger(context, texture, folder, "cap", CAP_COLOR),
						new TracksInteger(context, texture, folder, "spots", SPOTS_COLOR)
				);
				break;
			case INVERTED:
				builder.add(new TracksInteger(context, texture, folder, "spots", CAP_COLOR));
			case HARDCODED: // Switch-case fallthrough my beloved!!!!!!!!!!!
				builder.add(new TracksInteger(context, texture, folder, "cap", SPOTS_COLOR));
		}

		// If spots are hard-coded, then vest color always is too
		if(spots != SpotsMode.HARDCODED) builder.add(new TracksInteger(context, texture, folder, "vest", VEST_COLOR));

		builder.add(new TracksOptionalInteger(context, texture, folder, "shirt", SHIRT_COLOR));

		return builder.build();
	}

	private static class TracksInteger extends CustomizableTextureLayerFeature<Integer> {
		public TracksInteger(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, Identifier texture, String folder, String layer, TrackedData<Integer> trackedData) {
			super(context, texture, folder, layer, trackedData);
		}

		@Override
		protected int getColor(Integer trackedValue) {
			return trackedValue;
		}
	}

	private static class TracksOptionalInteger extends CustomizableTextureLayerFeature<OptionalInt> {
		public TracksOptionalInteger(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, Identifier texture, String folder, String layer, TrackedData<OptionalInt> trackedData) {
			super(context, texture, folder, layer, trackedData);
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

	private static class TracksIntegerAndBoolean extends CustomizableTextureLayerFeature<Integer> {
		private final TrackedData<Boolean> TRACKED_BOOLEAN;

		public TracksIntegerAndBoolean(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> context, Identifier texture, String folder, String layer, TrackedData<Integer> trackedInteger, TrackedData<Boolean> trackedBoolean) {
			super(context, texture, folder, layer, trackedInteger);
			this.TRACKED_BOOLEAN = trackedBoolean;
		}

		@Override
		public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, Integer trackedValue) {
			if(((CustomizablePlayerEntity) player).mqm$getCustomizationData(this.TRACKED_BOOLEAN))
				super.render(matrices, vertexConsumers, light, player, trackedValue);
		}

		@Override
		protected int getColor(Integer trackedValue) {
			return trackedValue;
		}
	}
}
