package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.util.RaccoonUtil;
import com.fqf.mario_qua_mario.forms.Raccoon;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;

public class RaccoonMarioClientAppearance extends AbstractMarioClientAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("raccoon_mario");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull Identifier getFormID() {
		return Raccoon.ID;
	}

	@Override
	public ModelPartData makeTorso(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData torso = super.makeTorso(root, helper);
		RaccoonUtil.addTail(torso, this.getTorsoSize(), new Vector2i(0, 70), helper);
		return torso;
	}

	@Override
	protected ModelPartData makeCapStateHead(ModelPartData headPart, AppearanceGeometryHelper helper, boolean hasCap) {
		ModelPartData capStateHead = super.makeCapStateHead(headPart, helper, hasCap);

		Vector3f earPivot = new Vector3f(this.getHeadSize().x / 2F - 2.5F, -this.getHeadSize().y, 1);
		if(!hasCap) earPivot.add(1, -1, 0);

		RaccoonUtil.addEars(
				capStateHead,
				earPivot,
				new Vector2i(16, 16), new Vector2i(16, 33), new Vector2i(38, 16),
				helper);
		return capStateHead;
	}

	@Override
	public void accumulateCustomFeatureRenderers(ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder, FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, EntityRendererFactory.Context ctx) {
		super.accumulateCustomFeatureRenderers(builder, featureRendererContext, ctx);
		builder.add(new RaccoonUtil.RaccoonFormEyesFeatureRenderer(featureRendererContext, this));
	}
}
