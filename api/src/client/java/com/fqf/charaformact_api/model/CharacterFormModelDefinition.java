package com.fqf.charaformact_api.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public interface CharacterFormModelDefinition {
	@NotNull Identifier getID();
	default @NotNull EntityModelLayer getModelLayer() {
		return new EntityModelLayer(this.getID(), "main");
	}

	@NotNull Vector2i getTextureSize();
	@NotNull Identifier getTextureLocation();

	default Vector3i getHeadSize() {
		return new Vector3i(8, 8, 8);
	}
	default Vector3i getTorsoSize() {
		return new Vector3i(8, 12, 4);
	}

	default Vector3i getArmSize() {
		return new Vector3i(4, 12, 4);
	}
	default Vector3i getLegSize() {
		return new Vector3i(4, 12, 4);
	}

	default int getYOffset() {
		return 24 - this.getLegSize().y - this.getTorsoSize().y;
	}

	default Vector2i getHeadUV() {
		return new Vector2i(0, 0);
	}

	default Vector2i getHatUV(CharacterFormModelHelper helper) {
		return new Vector2i(helper.getBottomRightCorner(getHeadUV(), getHeadSize()).x, 0);
	}

	default Vector2i getTorsoUV(CharacterFormModelHelper helper) {
		return new Vector2i(0, helper.getBottomRightCorner(getHeadUV(), getHeadSize()).y);
	}

	default Vector2i getJacketUV(CharacterFormModelHelper helper) {
		return new Vector2i(0, helper.getBottomRightCorner(getTorsoUV(helper), getTorsoSize()).y);
	}

	default Vector2i getRightLegUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				helper.getBottomRightCorner(getTorsoUV(helper), getTorsoSize()).x,
				helper.getBottomRightCorner(getHeadUV(), getHeadSize()).y
		);
	}

	default Vector2i getRightPantsUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				getRightLegUV(helper).x,
				helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).y
		);
	}

	default Vector2i getRightArmUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).x,
				helper.getBottomRightCorner(getHeadUV(), getHeadSize()).y
		);
	}

	default Vector2i getRightSleeveUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				getRightArmUV(helper).x,
				helper.getBottomRightCorner(getRightArmUV(helper), getArmSize()).y
		);
	}

	default ModelPartData makeHead(ModelPartData root, CharacterFormModelHelper helper) {
		Vector3i headSize = this.getHeadSize();
		return helper.makePartAndHat(
				root, false, EntityModelPartNames.HEAD, EntityModelPartNames.HAT,
				new Vector3f(0, getYOffset(), 0), // pivot
				new Vector3f(headSize.x / -2F, -headSize.y, headSize.z / -2F), // offset
				0, // mirrorable offset
				headSize, getHeadUV(), getHatUV(helper), true
		);
	}

	default ModelPartData makeTorso(ModelPartData root, CharacterFormModelHelper helper) {
		Vector3i torsoSize = this.getTorsoSize();
		return helper.makePartAndHat(
				root, false, EntityModelPartNames.BODY, EntityModelPartNames.JACKET,
				new Vector3f(0, getYOffset(), 0), // pivot
				new Vector3f(torsoSize.x / -2F, 0, torsoSize.z / -2F), // offset
				0, // mirrorable offset
				torsoSize, getTorsoUV(helper), getJacketUV(helper), true
		);
	}

	default ModelPartData makeArm(ModelPartData root, CharacterFormModelHelper helper, boolean isLeft) {
		Vector3i torsoSize = this.getTorsoSize();
		Vector3i armSize = this.getArmSize();
		return helper.makePartAndHat(
				root, isLeft,
				isLeft ? EntityModelPartNames.LEFT_ARM : EntityModelPartNames.RIGHT_ARM,
				isLeft ? CharacterFormModelHelper.LEFT_SLEEVE : CharacterFormModelHelper.RIGHT_SLEEVE,
				new Vector3f(torsoSize.x / -2F + armSize.x / -4F, armSize.y / 6F + getYOffset(), 0), // pivot
				new Vector3f(armSize.x / -2F, armSize.y / -6F, armSize.z / -2F), // offset
				armSize.x / -4F, // mirrorable offset
				getArmSize(), getRightArmUV(helper), getRightSleeveUV(helper), true
		);
	}

	default ModelPartData makeLeg(ModelPartData root, CharacterFormModelHelper helper, boolean isLeft) {
		Vector3i torsoSize = this.getTorsoSize();
		Vector3i legSize = this.getLegSize();
		return helper.makePartAndHat(
				root, isLeft,
				isLeft ? EntityModelPartNames.LEFT_LEG : EntityModelPartNames.RIGHT_LEG,
				isLeft ? CharacterFormModelHelper.LEFT_PANTS : CharacterFormModelHelper.RIGHT_PANTS,
				new Vector3f(torsoSize.x / 4F, torsoSize.y + getYOffset(), 0), // pivot
				new Vector3f(legSize.x / -2F, 0, legSize.z / -2F), // offset
				legSize.x / 40F, // mirrorable offset
				getLegSize(), getRightLegUV(helper), getRightPantsUV(helper), true
		);
	}

	default ModelPartData makeCape(ModelPartData root, CharacterFormModelHelper helper) {
		return helper.makePart(
				root, CharacterFormModelHelper.CAPE, false,
				new Vector3f(0, getYOffset(), 0), new Vector3f(-5, 0, -1),
				0, new Vector3i(10, 16, 1), new Vector2i()
		);
	}

	default TexturedModelData getTexturedModelData(CharacterFormModelHelper helper) {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		this.makeHead(root, helper);
		this.makeTorso(root, helper);

		this.makeArm(root, helper, false);
		this.makeArm(root, helper, true);

		this.makeLeg(root, helper, false);
		this.makeLeg(root, helper, true);

		helper.addUnusedEasterEggEarThatYouWillNeverSee(root);
		this.makeCape(root, helper);

		return TexturedModelData.of(modelData, 64, 64);
	}
}
