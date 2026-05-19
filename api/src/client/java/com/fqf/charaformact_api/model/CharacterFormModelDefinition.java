package com.fqf.charaformact_api.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public interface CharacterFormModelDefinition extends CommonSidedCharaFormModelDefinition {
	default @NotNull EntityModelLayer getModelLayer() {
		return new EntityModelLayer(this.getID(), "main");
	}

	@NotNull Vector2i getTextureSize();
	@NotNull Identifier getTextureLocation();

	// Methods for getting the size of vanilla parts.
	// The default implementation imitates vanilla proportions. Note: getLegSize is common-sided, as it is expected to
	// be used for getStrideDistance, which the server needs to know. Sorry. I know it's wonky.
	default Vector3i getHeadSize() {
		return new Vector3i(8, 8, 8);
	}
	default Vector3i getTorsoSize() {
		return new Vector3i(8, 12, 4);
	}

	// The pivot of every part attached to root should be offset by this value. The default implementation does that.
	default int getYOffset() {
		return 24 - this.getLegSize().y - this.getTorsoSize().y;
	}

	// Methods for getting the UVs of vanilla parts.
	// The default implementation imitates the vanilla skin format, but of course adjusted to account for the size of
	// the vanilla parts. Note that the default implementation also does not include any UV calculations for a left arm
	// or left leg, since the default part creation implementation simply mirrors the textures of the right arm and leg.
	default Vector2i getHeadUV() {
		return new Vector2i(0, 0);
	}
	default Vector2i getHatUV(CharacterFormModelHelper helper) {
		return new Vector2i(helper.getBottomRightCorner(getHeadUV(), getHeadSize()).x, 0);
	}
	default Vector2i getRightLegUV(CharacterFormModelHelper helper) {
		return new Vector2i(0, helper.getBottomRightCorner(getHeadUV(), getHeadSize()).y);
	}
	default Vector2i getRightPantsUV(CharacterFormModelHelper helper) {
		return new Vector2i(0, helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).y);
	}
	default Vector2i getTorsoUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).x,
				this.getRightLegUV(helper).y
		);
	}
	default Vector2i getJacketUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				getRightLegUV(helper).x,
				helper.getBottomRightCorner(getTorsoUV(helper), getTorsoSize()).y
		);
	}
	default Vector2i getRightArmUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).x,
				this.getRightLegUV(helper).y
		);
	}
	default Vector2i getRightSleeveUV(CharacterFormModelHelper helper) {
		return new Vector2i(
				getRightArmUV(helper).x,
				helper.getBottomRightCorner(getRightArmUV(helper), getArmSize()).y
		);
	}

	// Methods for selecting the position of the vanilla parts (specifically, their pivot points).
	// The default imitation imitates the vanilla player's body plan. Arms attached at the shoulders and so on.
	// It automatically accounts for the proportions given by the size getting methods, so you only have to override
	// these methods if you want the parts to attach somewhere different.
	// Note: The vanilla arms have a slightly unexpected pivot point that is not centered on the X axis. It is instead
	// shifted a bit closer towards the body, and an offset is applied to push the visible geometry away in turn.
	// This is likely intended to make shoulder rotations look more natural. Please be aware that this affects the
	// default implementations of getRightArmPivot and makeArm, as both of these methods imitate this vanilla feature.
	default Vector3f getHeadPivot() {
		return new Vector3f(0, this.getYOffset(), 0);
	}
	default Vector3f getTorsoPivot() {
		return new Vector3f(0, this.getYOffset(), 0);
	}
	default Vector3f getRightArmPivot() {
		Vector3i torsoSize = this.getTorsoSize();
		Vector3i armSize = this.getArmSize();
		return new Vector3f(torsoSize.x / -2F + armSize.x / -4F, armSize.y / 6F + getYOffset(), 0);
	}
	default Vector3f getRightLegPivot() {
		Vector3i torsoSize = this.getTorsoSize();
		return new Vector3f(torsoSize.x / -4F, torsoSize.y + getYOffset(), 0);
	}

	// Methods for creating the vanilla parts.
	// The default implementation creates two cuboids per part: One for the actual part, and another for its 3D layer
	// (hat, jacket, sleeve, or pants). You only need to override any of these if you want to replace the geometry
	// of a part entirely in some way, such as by splitting the torso into multiple cuboids.
	default ModelPartData makeHead(ModelPartData root, CharacterFormModelHelper helper) {
		Vector3i headSize = this.getHeadSize();
		return helper.makePartAndHat(
				root, false, EntityModelPartNames.HEAD, EntityModelPartNames.HAT,
				this.getHeadPivot(), // pivot
				new Vector3f(headSize.x / -2F, -headSize.y, headSize.z / -2F), // offset
				0, // mirrorable offset
				headSize, getHeadUV(), getHatUV(helper), true
		);
	}
	default ModelPartData makeTorso(ModelPartData root, CharacterFormModelHelper helper) {
		Vector3i torsoSize = this.getTorsoSize();
		return helper.makePartAndHat(
				root, false, EntityModelPartNames.BODY, EntityModelPartNames.JACKET,
				this.getTorsoPivot(), // pivot
				new Vector3f(torsoSize.x / -2F, 0, torsoSize.z / -2F), // offset
				0, // mirrorable offset
				torsoSize, getTorsoUV(helper), getJacketUV(helper), true
		);
	}
	default ModelPartData makeArm(ModelPartData root, CharacterFormModelHelper helper, boolean isLeft) {
		Vector3i armSize = this.getArmSize();
		return helper.makePartAndHat(
				root, isLeft,
				isLeft ? EntityModelPartNames.LEFT_ARM : EntityModelPartNames.RIGHT_ARM,
				isLeft ? CharacterFormModelHelper.LEFT_SLEEVE : CharacterFormModelHelper.RIGHT_SLEEVE,
				this.getRightArmPivot(), // pivot
				new Vector3f(armSize.x / -2F, armSize.y / -6F, armSize.z / -2F), // offset
				armSize.x / -4F, // mirrorable offset
				getArmSize(), getRightArmUV(helper), getRightSleeveUV(helper), true
		);
	}
	default ModelPartData makeLeg(ModelPartData root, CharacterFormModelHelper helper, boolean isLeft) {
		Vector3i legSize = this.getLegSize();
		return helper.makePartAndHat(
				root, isLeft,
				isLeft ? EntityModelPartNames.LEFT_LEG : EntityModelPartNames.RIGHT_LEG,
				isLeft ? CharacterFormModelHelper.LEFT_PANTS : CharacterFormModelHelper.RIGHT_PANTS,
				this.getRightLegPivot(), // pivot
				new Vector3f(legSize.x / -2F, 0, legSize.z / -2F), // offset
				legSize.x / 40F, // mirrorable offset
				getLegSize(), getRightLegUV(helper), getRightPantsUV(helper), true
		);
	}

	// CharaFormAct does not affect the player's cape, so it's generally best to make one that matches the vanilla cape.
	// That means you aren't providing the texture! It uses a vanilla texture! So if you change the UV or the size of
	// its cuboid, it'll probably mess it up and look gross. I wouldn't recommend overriding this method.
	// The cape probably won't play nice with custom models anyway.
	default ModelPartData makeCape(ModelPartData root, CharacterFormModelHelper helper) {
		return helper.makePart(
				root, CharacterFormModelHelper.CAPE, false,
				new Vector3f(0, getYOffset(), 0), new Vector3f(-5, 0, -1),
				0, new Vector3i(10, 16, 1), new Vector2i()
		);
	}

	// Please feel free to override this to return a custom class extending CharaFormEntityModel, if you like.
	// This will give you lots of control over the player's rendering logic.
	// If you do, I'd recommend still calling super on any methods you override.
	default CharacterFormEntityModel createModel(ModelPart root) {
		return new CharacterFormEntityModel(root);
	}

	// Methods for deciding where on the arm held items will render.
	// Default implementation imitates vanilla logic, although getHeldShieldPosition has special behavior to prevent
	// an issue where especially short-armed models (~4 px) would hold a shield above their shoulder.
	default Vector3f getHeldItemPosition() {
		Vector3i armSize = this.getArmSize();
		return new Vector3f(0.25F * armSize.x, armSize.y * -0.8333333F, 0.5F * armSize.z);
	}
	default Vector3f getHeldShieldPosition() {
		Vector3f heldItemPosition = this.getHeldItemPosition();
		Vector3i armSize = this.getArmSize();

		return new Vector3f(heldItemPosition.x, Math.min(heldItemPosition.y, -3.25F - armSize.y / 2F), heldItemPosition.z);
	}

	// Method for positioning shoulder parrots.
	// Unfortunately I don't think this applies to other shoulder mounts such as Cobblemon. :(
	// Parrots always render 24 pixels below the shoulder, regardless of the scaling of any model parts. As a result,
	// the Y position given by the default implementation does not scale either.
	default Vector3f getShoulderParrotPosition() {
		return new Vector3f(Math.max(this.getTorsoSize().x, this.getHeadSize().x) / 2F + 2.4F, -24.0F, 0);
	}

	// Methods for transforming features on various parts of the body such as armor and other equipment.
	// This is meant to be maximally compatible. Default implementations tries to maintain armor's aspect ratio when
	// possible, and attempts sensible defaults for other features.
	// The elaborate logic in all of these default implementations is only necessary to account for the unknown size of
	// the body parts. If you're overriding any of these in your own model, it would make more sense to just return a
	// FeatureTransformationInstructions object constructed from 9 raw, hard-coded floats, without doing any
	// calculations at all. That said, these methods only run a single time while preparing the model data during
	// startup, and are then cached forever, so there is no performance cost to any logic done here.
	default FeatureTransformationInstructions getHelmetTransformation() {
		Vector3i headSize = this.getHeadSize();
		Vector3f scale;
		if(Math.abs(headSize.x - headSize.z) <= 2) {
			// We can preserve the horizontal aspect ratio, so maybe we can keep the vertical ratio intact too!
			int horizontalSize = Math.max(headSize.x, headSize.z);
			float horizontalScale = horizontalSize / 8F;
			if(headSize.y >= horizontalSize - 2) scale = new Vector3f(horizontalScale, horizontalScale, horizontalScale);
			else scale = new Vector3f(horizontalScale, headSize.y / 8F, horizontalScale);
		}
		else scale = new Vector3f(headSize.x / 8F, headSize.y / 8F, headSize.z / 8F);

		return new FeatureTransformationInstructions(
				0, headSize.y - 8 + 8 * (1 - scale.y), 0,
				0, 0, 0,
				scale.x, scale.y, scale.z
		);
	}
	default FeatureTransformationInstructions getHatTransformation() {
		// Not the 3D hat layer. This is for mods which add hats, such as the Villager Hats or Simple Hats mods.
		return this.getHelmetTransformation();
	}
	default FeatureTransformationInstructions getUnknownHeadFeatureTransformation() {
		// Transformation to apply to features which attach to the head but are otherwise unknown.
		Vector3i headSize = this.getHeadSize();
		return new FeatureTransformationInstructions(
				0, headSize.y - 8, 0,
				0, 0, 0,
				headSize.x / 8F, headSize.y / 8F, headSize.z / 8F
		);
	}
	default FeatureTransformationInstructions getCuirassTransformation() {
		// A cuirass is the largest part of a chestplate, that covers the whole torso.
		Vector3i torsoSize = this.getTorsoSize();
		float horizontalScale;
		Vector3f scale;
		if(Math.abs(torsoSize.x - 2 * torsoSize.z) <= 2) {
			horizontalScale = Math.max(torsoSize.x, 2 * torsoSize.z) / 8F;
			if(torsoSize.y >= horizontalScale * 9) scale = new Vector3f(horizontalScale, horizontalScale, horizontalScale);
			else scale = new Vector3f(horizontalScale, torsoSize.y / 12F, horizontalScale);
		}
		else {
			horizontalScale = torsoSize.x / 8F;
			if(torsoSize.y >= horizontalScale * 9)
				scale = new Vector3f(horizontalScale, horizontalScale, torsoSize.z / 4F);
			else
				scale = new Vector3f(torsoSize.x / 8F, torsoSize.y / 12F, torsoSize.z / 4F);
		}
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				scale.x, scale.y, scale.z
		);
	}
	default FeatureTransformationInstructions getFauldTransformation() {
		// A fauld is a piece of armor for protecting the hip. It's the highest cuboid of vanilla leggings.
		Vector3i torsoSize = this.getTorsoSize();
		FeatureTransformationInstructions cuirassTransformation = this.getCuirassTransformation();
		return new FeatureTransformationInstructions(
				cuirassTransformation.forwards(),
				cuirassTransformation.upwards() + 12 - torsoSize.y - 12 * (1 - cuirassTransformation.yScale()),
				cuirassTransformation.rightwards(),
				cuirassTransformation.pitch(), cuirassTransformation.yaw(), cuirassTransformation.roll(),
				cuirassTransformation.xScale(), cuirassTransformation.yScale(), cuirassTransformation.zScale()
		);
	}
	default FeatureTransformationInstructions getBackEquipmentTransformation() {
		// Applies to things like Elytra and modded backpacks.
		Vector3i torsoSize = this.getTorsoSize();
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}
	default FeatureTransformationInstructions getUnknownChestFeatureTransformation() {
		// Applies to things like Elytra and modded backpacks.
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}
	default FeatureTransformationInstructions getPauldronTransformation() {
		// Applies to the shoulder piece of a chestplate.
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}
	default FeatureTransformationInstructions getGlovesTransformation() {
		// Applies to gloves from mods, such as from The Aether.
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}
	default FeatureTransformationInstructions getUnknownArmsFeatureTransformation() {
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}
	default FeatureTransformationInstructions getBootsTransformation() {
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}
	default FeatureTransformationInstructions getChaussesTransformation() {
		// Chausses are the part of the leggings that guards the legs.
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}
	default FeatureTransformationInstructions getUnknownLegsFeatureTransformation() {
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				1, 1, 1
		);
	}

	default ModelData getModelData(CharacterFormModelHelper helper) {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		this.makeHead(root, helper);
		this.makeTorso(root, helper);

		this.makeArm(root, helper, false);
		this.makeArm(root, helper, true);

		this.makeLeg(root, helper, false);
		this.makeLeg(root, helper, true);

		this.makeCape(root, helper);

		return modelData;
	}
}
