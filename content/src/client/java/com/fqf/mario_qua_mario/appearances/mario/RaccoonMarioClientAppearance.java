package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.util.RaccoonUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class RaccoonMarioClientAppearance extends SuperMarioClientAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("raccoon_mario");

	@Override
	public ModelPartData makeTorso(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData torso = super.makeTorso(root, helper);
		RaccoonUtil.addTail(torso, this.getTorsoSize(), new Vector2i(0, 70), helper);
		return torso;
	}

	@Override
	protected ModelPartData makeCapStateHead(ModelPartData headPart, AppearanceGeometryHelper helper, boolean hasCap) {
		ModelPartData capStateHead = super.makeCapStateHead(headPart, helper, hasCap);

		RaccoonUtil.addEars(
				capStateHead,
				new Vector3f(this.getHeadSize().x / 2F - 2.5F, -this.getHeadSize().y, 1), // pivot
				new Vector2i(16, 16), new Vector2i(16, 33), new Vector2i(38, 16), // UVs
				helper
		);
		return capStateHead;
	}

	@Override
	public void accumulateCustomFeatureRenderers(Identifier texture, ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder, FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, EntityRendererFactory.Context ctx) {
		super.accumulateCustomFeatureRenderers(texture, builder, featureRendererContext, ctx);
		builder.add(new RaccoonUtil.RaccoonFormEyesFeatureRenderer(featureRendererContext, texture));
	}
}
