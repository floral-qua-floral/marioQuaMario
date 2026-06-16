package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public abstract class PlumberClientAppearance implements ClientAppearanceDefinition {
	public static final String CAPLESS_HEAD = "capless";
	public static final String CAPFUL_HEAD = "capful";
	public static final String CAP_TOP = "cap_top";
	public static final String CAP_BRIM = "cap_brim";
	public static final String CAP_EMBLEM = "emblem";

	@Override
	public ModelPartData makeHead(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData head = helper.makeInvisiblePart(root, EntityModelPartNames.HEAD, this.getHeadPivot(), false);
		helper.makeInvisiblePart(root, EntityModelPartNames.HAT, this.getHeadPivot(), false);
		this.makeCapStates(head, helper);
		return head;
	}

	@Override
	public AppearanceModel createModel(ModelPart root) {
		return new PlumberAppearanceModel(root);
	}

	protected Vector3i getNoseSize() {
		return new Vector3i(3, 2, 2);
	}
	protected Vector3i getCapBrimSize() {
		return new Vector3i(this.getHeadSize().x, 1, 2);
	}
	protected Vector3i getCapTopSize() {
		return new Vector3i(this.getHeadSize().x, 1, 4);
	}

	protected Vector2i getCapfulHeadUV(AppearanceGeometryHelper helper) {
		return new Vector2i(0, Math.max(
				helper.getBottomRightCorner(this.getRightPantsUV(helper), this.getLegSize()).y,
				Math.max(
						helper.getBottomRightCorner(this.getJacketUV(helper), this.getTorsoSize()).y,
						helper.getBottomRightCorner(this.getRightSleeveUV(helper), this.getArmSize()).y
				)
		));
	}

	protected Vector2i getCapfulHatUV(AppearanceGeometryHelper helper) {
		Vector2i capfulHeadUV = this.getCapfulHeadUV(helper);
		return new Vector2i(helper.getBottomRightCorner(capfulHeadUV, this.getHeadSize()).x, capfulHeadUV.y);
	}
	protected Vector2i getNoseUV(AppearanceGeometryHelper helper) {
		return new Vector2i(0, helper.getBottomRightCorner(this.getCapfulHeadUV(helper), this.getHeadSize()).y);
	}
	protected Vector2i getCapBrimUV(AppearanceGeometryHelper helper) {
		Vector2i noseUV = this.getNoseUV(helper);
		return new Vector2i(helper.getBottomRightCorner(noseUV, this.getNoseSize()).x, noseUV.y);
	}
	protected Vector2i getCapTopUV(AppearanceGeometryHelper helper) {
		Vector2i brimUV = this.getCapBrimUV(helper);
		return new Vector2i(helper.getBottomRightCorner(brimUV, this.getCapBrimSize()).x, brimUV.y);
	}

	protected String getSkinForm() {
		return "super";
	}

	@Override
	public void accumulateCustomFeatureRenderers(ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder, FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, EntityRendererFactory.Context ctx) {
		builder.add(CustomizableTextureLayerFeature.makeOptionalSkinFeature(featureRendererContext, this.getSkinForm(), this));
	}

	protected void makeCapStates(ModelPartData head, AppearanceGeometryHelper helper) {
		this.makeCapStateHead(head, helper, true);
		this.makeCapStateHead(head, helper, false);
	}

	protected ModelPartData makeCapStateHead(ModelPartData headPart, AppearanceGeometryHelper helper, boolean hasCap) {
		Vector3i headSize = this.getHeadSize();
		ModelPartData capStateHead = helper.makePartAndHat(
				headPart, false, hasCap ? CAPFUL_HEAD : CAPLESS_HEAD, EntityModelPartNames.HAT,
				new Vector3f(), // pivot (handled with root part)
				new Vector3f(headSize.x / -2F, -headSize.y, headSize.z / -2F), // offset
				0, // mirrorable offset
				new Vector3f(), headSize,
				hasCap ? this.getCapfulHeadUV(helper) : this.getHeadUV(),
				hasCap ? this.getCapfulHatUV(helper) : this.getHatUV(helper),
				false
		);
		addNose(capStateHead, this.getHeadSize(), this.getNoseSize(), this.getNoseUV(helper), helper);
		if(hasCap) {
			Vector2i topUV = this.getCapTopUV(helper);
			Vector3i topSize = this.getCapTopSize();
			Vector2i brimUV = this.getCapBrimUV(helper);
			Vector3i brimSize = this.getCapBrimSize();
			helper.makePart(
					capStateHead, CAP_TOP, false,
					new Vector3f(0, -headSize.y, headSize.z / -2F + topSize.z / 2F), // pivot
					new Vector3f(topSize.x / -2F, -topSize.y, topSize.z / -2F), // offset
					0, // mirrorable offset
					new Vector3f(), topSize, topUV
			);
			helper.makePart(
					capStateHead, CAP_BRIM, false,
					new Vector3f(0, -headSize.y + 1 + brimSize.y / 2F, headSize.z / -2F), // pivot
					new Vector3f(brimSize.x / -2F, brimSize.y / -2F, -brimSize.z), // offset
					0, // mirrorable offset
					new Vector3f(), brimSize, brimUV
			);
		}
		return capStateHead;
	}

	public static void addNose(ModelPartData head, Vector3i headSize, Vector3i noseSize, Vector2i noseUV, AppearanceGeometryHelper helper) {
		helper.makePart(
				head,
				EntityModelPartNames.NOSE,
				false,
				new Vector3f(0, -3, headSize.z / -2F), // pivot
				new Vector3f(noseSize.x / -2F, noseSize.y / -2F, -noseSize.z), // offset
				0, // mirrorable offset
				new Vector3f(), noseSize, noseUV
		);
	}
}
