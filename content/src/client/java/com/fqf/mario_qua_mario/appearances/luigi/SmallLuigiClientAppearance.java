package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.util.CustomizableTextureLayerFeature;
import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import com.fqf.mario_qua_mario.characters.Luigi;
import com.fqf.mario_qua_mario.forms.Small;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class SmallLuigiClientAppearance implements ClientAppearanceDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("small_luigi");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Luigi.ID;
	}
	@Override public @NotNull Identifier getFormID() {
		return Small.ID;
	}

	@Override public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 64);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(8, 4, 5);
	}

	@Override
	public Vector3i getArmSize() {
		return SmallLuigiCommonAppearance.ARM_SIZE;
	}

	@Override
	public Vector3i getLegSize() {
		return SmallLuigiCommonAppearance.LEG_SIZE;
	}

	@Override
	public ModelPartData makeHead(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData head = ClientAppearanceDefinition.super.makeHead(root, helper);
		PlumberClientAppearance.addNose(head, this.getHeadSize(), new Vector3i(3, 2, 2), new Vector2i(52, 16), helper);
		return head;
	}

	@Override
	public void accumulateCustomFeatureRenderers(ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder, FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, EntityRendererFactory.Context ctx) {
		builder.add(CustomizableTextureLayerFeature.makeOptionalSkinFeature(featureRendererContext, "small", this));
	}
}
