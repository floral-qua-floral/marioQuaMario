package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.*;
import com.fqf.mario_qua_mario.appearances.util.CustomizableTextureLayerFeature;
import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class MiniMarioClientAppearance extends MiniMarioCommonAppearance implements ClientAppearanceDefinition {
	@Override
	public @NotNull Vector2i defineTextureSize() {
		return new Vector2i(32, 32);
	}

	@Override
	public Vector3i getHeadSize() {
		return new Vector3i(4, 4, 4);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(4, 2, 3);
	}

	@Override
	public ModelPartData makeHead(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData head = ClientAppearanceDefinition.super.makeHead(root, helper);
		ModelPartData brim = helper.makePart(
				head, PlumberClientAppearance.CAP_BRIM, false,
				new Vector3f(0, -3, -2), // pivot
				new Vector3f(-2, 0, -1), // offset
				0, // mirrorable offset
				new Vector3f(), // rotation
				new Vector3i(4, 0, 1), // size
				new Vector2i(11, 0) // uv
		);
		helper.makePart(
				brim, "brim_underside", false,
				new Vector3f(0, 0.005F, 0),
				new Vector3f(-2, 0, -1),
				0,
				new Vector3f(),
				new Vector3i(4, 0, 1),
				new Vector2i(11, 1)
		);
		helper.makePart(
				head, EntityModelPartNames.NOSE, false,
				new Vector3f(0, -1.5F, -2), // pivot
				new Vector3f(-1, 0, -1), // offset
				0, // mirrorable offset
				new Vector3f(), // rotation
				new Vector3i(2, 1, 1), // size
				new Vector2i(12, 2) // uv
		);
		return head;
	}

	@Override
	public void accumulateCustomFeatureRenderers(Identifier texture, ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder, FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, EntityRendererFactory.Context ctx) {
		builder.add(CustomizableTextureLayerFeature.makeOptionalSkinFeature(featureRendererContext, texture, "mini"));
	}

	@Override
	public TransformationInstructions getBootsTransformation(AppearanceFeatureHelper helper) {
		return ClientAppearanceDefinition.super.getBootsTransformation(helper).offset(0, 1.5F, 0);
	}
}
