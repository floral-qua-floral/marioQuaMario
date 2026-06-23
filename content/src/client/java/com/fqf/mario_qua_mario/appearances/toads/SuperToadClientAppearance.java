package com.fqf.mario_qua_mario.appearances.toads;

import com.fqf.charaformact_api.appearance.*;
import com.fqf.mario_qua_mario.appearances.util.ToadAppearanceModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class SuperToadClientAppearance extends SuperToadCommonAppearance implements ClientAppearanceDefinition {
	public static final String CAP_BULB = "cap_bulb";
	public static final String CAP_TIP = "cap_tip";
	public static final String CAP_HAT = "cap_hat";

	public static final String RIGHT_PIGTAIL_TOP = "right_pigtail_top";
	public static final String LEFT_PIGTAIL_TOP = "left_pigtail_top";
	public static final String RIGHT_PIGTAIL_BOTTOM = "right_pigtail_bottom";
	public static final String LEFT_PIGTAIL_BOTTOM = "left_pigtail_bottom";
	public static final String RIGHT_PIGTAIL_TOP_SPOTS = "right_pigtail_top_spots";
	public static final String LEFT_PIGTAIL_TOP_SPOTS = "left_pigtail_top_spots";
	public static final String RIGHT_PIGTAIL_BOTTOM_SPOTS = "right_pigtail_bottom_spots";
	public static final String LEFT_PIGTAIL_BOTTOM_SPOTS = "left_pigtail_bottom_spots";

	@Override public @NotNull Vector2i defineTextureSize() {
		return new Vector2i(64, 96);
	}

	@Override
	public Vector3i getHeadSize() {
		return new Vector3i(8, 5, 8);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(8, 9, 4);
	}

	public Vector3i getCapBulbSize() {
		return new Vector3i(12, 7, 12);
	}
	public Vector2i getCapBulbUV(AppearanceGeometryHelper helper) {
		return new Vector2i(0, Math.max(
				helper.getBottomRightCorner(this.getRightPantsUV(helper), this.getLegSize()).y,
				Math.max(
						helper.getBottomRightCorner(this.getJacketUV(helper), this.getTorsoSize()).y,
						helper.getBottomRightCorner(this.getRightSleeveUV(helper), this.getArmSize()).y
				)
		));
	}

	public Vector3i getPigtailBottomSize() {
		return new Vector3i(4);
	}

	public Vector3f getBulbPivot() {
		return new Vector3f(0, -this.getHeadSize().y + 1.2F, 0.1F);
	}

	public Vector3f getPigtailTopPivot() {
		Vector3i bulbSize = this.getCapBulbSize();
		return new Vector3f(bulbSize.x / 2F - 1, -1, bulbSize.z / 2F - 1);
	}

	public Vector3f getBulbRotation() {
		return new Vector3f(6.0213857F, 0, 0);
	}

	public Vector2i getPigtailTopUV() {
		return new Vector2i(0, 39);
	}
	public Vector2i getPigtailBottomUV() {
		return new Vector2i(48, 39);
	}

	public void makePigtail(ModelPartData bulb, boolean isLeft, AppearanceGeometryHelper helper) {
		Vector3i bulbSize = this.getCapBulbSize();
		Vector3i bottomSize = this.getPigtailBottomSize();
		Vector3i topSize = new Vector3i(bottomSize).sub(1, 1, 1);
		Vector2i topUV = this.getPigtailTopUV();
		Vector2i bottomUV = this.getPigtailBottomUV();

		ModelPartData top = helper.makePartAndHat(
				bulb, isLeft,
				isLeft ? LEFT_PIGTAIL_TOP : RIGHT_PIGTAIL_TOP,
				isLeft ? LEFT_PIGTAIL_TOP_SPOTS : RIGHT_PIGTAIL_TOP_SPOTS,
				this.getPigtailTopPivot(), // pivot
				new Vector3f(topSize.x / -2F, 0, topSize.z / -2F + 0.5F), // offset
				0.5F, // mirrorable offset
				new Vector3f(0.2617994F, 0, 0), // rotation
				topSize, // size
				topUV, new Vector2i(topUV.x, helper.getBottomRightCorner(topUV, topSize).y),
				false
		);
		helper.makePartAndHat(
				top, isLeft,
				isLeft ? LEFT_PIGTAIL_BOTTOM : RIGHT_PIGTAIL_BOTTOM,
				isLeft ? LEFT_PIGTAIL_BOTTOM_SPOTS : RIGHT_PIGTAIL_BOTTOM_SPOTS,
				new Vector3f(0, topSize.y, 0), // pivot
				new Vector3f(bottomSize.x / -2F, 0, bottomSize.z / -2F + 1), // offset
				1, // mirrorable offset
				new Vector3f(), // rotation
				bottomSize, // size
				bottomUV, new Vector2i(bottomUV.x, helper.getBottomRightCorner(bottomUV, bottomSize).y),
				false
		);
	}

	@Override
	public ModelPartData makeHead(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData head = ClientAppearanceDefinition.super.makeHead(root, helper);

		Vector3i bulbSize = this.getCapBulbSize();
		Vector2i bulbUV = this.getCapBulbUV(helper);

		ModelPartData bulb = helper.makePart(
				head, CAP_BULB, false,
				this.getBulbPivot(), // pivot
				new Vector3f(bulbSize.x / -2F, -bulbSize.y, bulbSize.z / -2F), // offset
				0, // mirrorable offset
				this.getBulbRotation(), // rotation (radians)
				bulbSize, // size
				bulbUV // uv
		);
		Vector3i capTipSize = new Vector3i(bulbSize.x - 2, 1, bulbSize.z - 2);
		Vector2i capTipUV = new Vector2i(0, helper.getBottomRightCorner(bulbUV, bulbSize).y);
		helper.makePart(
				bulb, CAP_TIP, false,
				new Vector3f(0, -bulbSize.y, 0), // pivot
				new Vector3f(capTipSize.x / -2F, -1, capTipSize.z / -2F), // offset
				0, // mirrorable offset
				new Vector3f(), // rotation (radians)
				new Vector3i(bulbSize.x - 2, 1, bulbSize.z - 2), // size
				capTipUV // uv
		);
		helper.makePart(
				bulb, CAP_HAT, false,
				new Vector3f(), // pivot
				new Vector3f(bulbSize.x / -2F, -bulbSize.y - 1, bulbSize.z / -2F), // offset
				0, // mirrorable offset
				new Vector3f(), // rotation (radians)
				new Vector3i(bulbSize.x, bulbSize.y + 2, bulbSize.z), // size
				new Vector2i(0, helper.getBottomRightCorner(capTipUV, capTipSize).y), // uv
				0.25F
		);

		this.makePigtail(bulb, false, helper);
		this.makePigtail(bulb, true, helper);

		return head;
	}

	@Override
	public AppearanceModel createModel(ModelPart root) {
		return new ToadAppearanceModel(root);
	}

	@Override
	public TransformationInstructions getHelmetTransformation(AppearanceFeatureHelper helper) {
		Vector3i bulbSize = this.getCapBulbSize();
		Vector3f bulbPivot = this.getBulbPivot();
		float gap = 0.98F; // gap between the surface of the mushroom cap & the helmet
		float scale = ((bulbSize.x + bulbSize.z - 4 + gap * 4) / 2F) / 8F;

		float angle = this.getBulbRotation().x;

		Vector3f vanillaRotatedPivot = new Vector3f(0, 8 * scale, 0).rotateX(angle);
		float addend = MathHelper.lerp(gap, 0.2F, 0.4F); // why? why? why? why? why? why???
		Vector3f rotatedPivot = new Vector3f(0, bulbSize.y + addend + gap, 0).rotateX(angle);

		return new TransformationInstructions(
				-vanillaRotatedPivot.z - bulbPivot.z + rotatedPivot.z,
				-vanillaRotatedPivot.y - bulbPivot.y + rotatedPivot.y,
				-vanillaRotatedPivot.x - bulbPivot.x + rotatedPivot.x,
				angle, 0, 0,
				scale, scale, scale
		);
	}

	@Override
	public TransformationInstructions getUnknownHeadFeatureTransformation(AppearanceFeatureHelper helper) {
		Vector3i bulbSize = this.getCapBulbSize();

		return helper.getStretchingTransformation(
				new Vector3f(bulbSize.x - 0.8F, this.getHeadSize().y + bulbSize.y - 0.5F,  bulbSize.z - 0.5F),
				new Vector3i(8, 8, 8)
		).offset(-1, 0, 0);
	}
}
