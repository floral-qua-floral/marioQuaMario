package com.fqf.charaformact_api.appearance;

import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;

/**
 * An Appearance is essentially a playermodel that is associated with a single Character + Form intersection.
 */
public interface ClientAppearanceDefinition extends CommonAppearanceDefinition {
	default @NotNull EntityModelLayer getModelLayer() {
		return new EntityModelLayer(this.getID(), "main");
	}

	@NotNull Vector2i getTextureSize();
	default @NotNull Identifier getTextureLocation() {
		return Identifier.of(this.getID().getNamespace(), "textures/entity/player/appearance/"
				+ this.getCharacterID().getPath() + "/" + this.getFormID().getPath() + ".png");
	}

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
	default Vector2i getHatUV(AppearanceGeometryHelper helper) {
		return new Vector2i(helper.getBottomRightCorner(getHeadUV(), getHeadSize()).x, 0);
	}
	default Vector2i getRightLegUV(AppearanceGeometryHelper helper) {
		return new Vector2i(0, helper.getBottomRightCorner(getHeadUV(), getHeadSize()).y);
	}
	default Vector2i getRightPantsUV(AppearanceGeometryHelper helper) {
		return new Vector2i(0, helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).y);
	}
	default Vector2i getTorsoUV(AppearanceGeometryHelper helper) {
		return new Vector2i(
				helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).x,
				this.getRightLegUV(helper).y
		);
	}
	default Vector2i getJacketUV(AppearanceGeometryHelper helper) {
		return new Vector2i(
				getTorsoUV(helper).x,
				helper.getBottomRightCorner(getTorsoUV(helper), getTorsoSize()).y
		);
	}
	default Vector2i getRightArmUV(AppearanceGeometryHelper helper) {
		return new Vector2i(
				helper.getBottomRightCorner(getTorsoUV(helper), getTorsoSize()).x,
				this.getRightLegUV(helper).y
		);
	}
	default Vector2i getRightSleeveUV(AppearanceGeometryHelper helper) {
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
	// The default implementation creates two cuboids per part: One for the actual part, and another for its 3D layerPostureMutator
	// (hat, jacket, sleeve, or pants). You only need to override any of these if you want to replace the geometry
	// of a part entirely in some way, such as by splitting the torso into multiple cuboids.
	default ModelPartData makeHead(ModelPartData root, AppearanceGeometryHelper helper) {
		Vector3i headSize = this.getHeadSize();
		return helper.makePartAndHat(
				root, false, EntityModelPartNames.HEAD, EntityModelPartNames.HAT,
				this.getHeadPivot(), // pivot
				new Vector3f(headSize.x / -2F, -headSize.y, headSize.z / -2F), // offset
				0, // mirrorable offset
				new Vector3f(), headSize, getHeadUV(), getHatUV(helper), true
		);
	}
	default ModelPartData makeTorso(ModelPartData root, AppearanceGeometryHelper helper) {
		Vector3i torsoSize = this.getTorsoSize();
		return helper.makePartAndHat(
				root, false, EntityModelPartNames.BODY, EntityModelPartNames.JACKET,
				this.getTorsoPivot(), // pivot
				new Vector3f(torsoSize.x / -2F, 0, torsoSize.z / -2F), // offset
				0, // mirrorable offset
				new Vector3f(), torsoSize, getTorsoUV(helper), getJacketUV(helper), true
		);
	}
	default ModelPartData makeArm(ModelPartData root, AppearanceGeometryHelper helper, boolean isLeft) {
		Vector3i armSize = this.getArmSize();
		return helper.makePartAndHat(
				root, isLeft,
				isLeft ? EntityModelPartNames.LEFT_ARM : EntityModelPartNames.RIGHT_ARM,
				isLeft ? AppearanceGeometryHelper.LEFT_SLEEVE : AppearanceGeometryHelper.RIGHT_SLEEVE,
				this.getRightArmPivot(), // pivot
				new Vector3f(armSize.x / -2F, armSize.y / -6F, armSize.z / -2F), // offset
				armSize.x / -4F, // mirrorable offset
				new Vector3f(), getArmSize(), getRightArmUV(helper), getRightSleeveUV(helper), true
		);
	}
	default ModelPartData makeLeg(ModelPartData root, AppearanceGeometryHelper helper, boolean isLeft) {
		Vector3i legSize = this.getLegSize();
		return helper.makePartAndHat(
				root, isLeft,
				isLeft ? EntityModelPartNames.LEFT_LEG : EntityModelPartNames.RIGHT_LEG,
				isLeft ? AppearanceGeometryHelper.LEFT_PANTS : AppearanceGeometryHelper.RIGHT_PANTS,
				this.getRightLegPivot(), // pivot
				new Vector3f(legSize.x / -2F, 0, legSize.z / -2F), // offset
				legSize.x / 40F, // mirrorable offset
				new Vector3f(), getLegSize(), getRightLegUV(helper), getRightPantsUV(helper), true
		);
	}

	// Please feel free to override this to return a custom class extending CharaFormEntityModel, if you like.
	// This will give you lots of control over the player's rendering logic.
	// If you do, I'd recommend still calling super on any methods you override.
	default AppearanceModel createModel(ModelPart root) {
		return new AppearanceModel(root);
	}

	// Please feel free to override this to add any custom feature renderers you would like. For instance, you could use
	// this to add a glowing element onto your model. You should not use this for extra body parts such as a tail;
	// instead, add things like that to getModelData as genuine ModelParts. You can then animate these custom parts
	// yourself by overriding createModel with a custom model class, then animate the parts in its preActionAnimation or
	// postActionAnimation methods.
	default List<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> getFeatureRenderersToAdd(
			FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext,
			EntityRendererFactory.Context ctx
	) {
		return List.of();
	}

	// Methods for deciding where on the arm held items will render (third person).
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

	// Methods for positioning, rotating, and scaling the arm in first person.
	// Default implementation translates the arm to put the shoulder of it at the same (off-screen) position as the
	// vanilla first-person arm. If the arm is so short that this would put the whole arm off-screen, it is then
	// adjusted back the other way to try and make sure the fist will still be visible.
	// Due to some sort of scary matrix math, the First Person arm doesn't actually rotate around its pivot point?
	default TransformationInstructions getEmptyFpHandTransformation() {
		return TransformationInstructions.VANILLA.offset(
				0,
				this.getYOffset() - Math.max(0, 9.5F - this.getArmSize().y),
				0
		);
	}
	// The "With Map" version is used when the arm being transformed is holding an item. In vanilla, this is exclusively
	// relevant for Filled Maps, hence the name. This is because the first-person hand does not render at all when
	// holding any other item. The reason this applies to all items is to try and improve compatibility with Hold My
	// Items and Punchy. As such, it's recommended that you try to maintain the default behavior if you must override
	// the "With Map" method.
	default TransformationInstructions getFpHandWithMapTransformation() {
		return TransformationInstructions.VANILLA.offset(
				0,
				this.getYOffset() + this.getArmSize().y - 12,
				0
		);
	}

	// Methods for transforming features on various parts of the body, such as armor and other equipment.
	// This is meant to be maximally compatible. Default implementations tries to maintain armor's aspect ratio when
	// possible, and attempts sensible defaults for other features.
	// The elaborate logic in all of these default implementations is only necessary to account for the unknown size of
	// the body parts. If you're overriding any of these in your own model, it would make more sense to just return a
	// TransformationInstructions object constructed from 9 raw, hard-coded floats, without doing any
	// calculations at all. That said, these methods only run a single time while preparing the model data during
	// startup, and are then cached forever, so there is no performance cost to any logic done here.
	default TransformationInstructions getHelmetTransformation(AppearanceFeatureHelper helper) {
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

		return new TransformationInstructions(
				0, headSize.y - 8 + 8 * (1 - scale.y), 0,
				0, 0, 0,
				scale.x, scale.y, scale.z
		);
	}
	default TransformationInstructions getHatTransformation(AppearanceFeatureHelper helper) {
		// Not the 3D hat layerPostureMutator. This is for mods which add hats, such as the Villager Hats or Simple Hats mods.
		return this.getHelmetTransformation(helper);
	}
	default TransformationInstructions getUnknownHeadFeatureTransformation(AppearanceFeatureHelper helper) {
		// Transformation to apply to features which attach to the head but are otherwise unknown.
		return helper.getStretchingTransformation(this.getHeadSize(), new Vector3i(8, 8, 8));
	}
	default TransformationInstructions getCuirassTransformation(AppearanceFeatureHelper helper) {
		// A cuirass is the largest part of a chestplate, that covers the whole torso.
		Vector3i torsoSize = this.getTorsoSize();

		float xScale, yScale, zScale;

		// Must ALWAYS perfectly match torso width.
		xScale = torsoSize.x / 8F;

		// Match aspect ratio on Z if possible.
		int torsoDoubleDepth = torsoSize.z * 2;
		if(torsoDoubleDepth <= xScale && torsoDoubleDepth >= xScale - 2)
			zScale = xScale;
		else
			zScale = torsoSize.z / 4F;

		// Match aspect ratio on Y if possible.
		float desirableCuirassHeight = 12 * xScale;
		if(torsoSize.y >= desirableCuirassHeight)
			//noinspection SuspiciousNameCombination
			yScale = xScale;
		else
			yScale = torsoSize.y / 12F;

		return new TransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				xScale, yScale, zScale
		);
	}
	default TransformationInstructions getFauldTransformation(AppearanceFeatureHelper helper) {
		// A fauld is a piece of armor for protecting the hip. It's the highest cuboid of vanilla leggings.
		return this.getCuirassTransformation(helper).flip(this.getTorsoSize(), 12);
	}
	default TransformationInstructions getBackEquipmentTransformation(AppearanceFeatureHelper helper) {
		// Applies to things like Elytra and modded backpacks.
		Vector3i torsoSize = this.getTorsoSize();
		float scale = Math.min(1, (torsoSize.y + this.getLegSize().y) / 12F);
		return new TransformationInstructions(
				2 - torsoSize.z / 2F, 0, 0,
				0, 0, 0,
				scale, scale, scale
		);
	}
	default TransformationInstructions getUnknownChestFeatureTransformation(AppearanceFeatureHelper helper) {
		// Applies to things like Elytra and modded backpacks.
		return helper.getStretchingTransformation(this.getTorsoSize(), new Vector3i(8, 12, 4));
	}
	default TransformationInstructions getPauldronTransformation(AppearanceFeatureHelper helper) {
		// Applies to the shoulder piece of a chestplate.
		Vector3i armSize = this.getArmSize();
		TransformationInstructions base = helper.getArmorTransformation(
				armSize, new Vector3i(4, 12, 4), 2, 0.25F);

		return base.offset(0, Math.max(0, armSize.y / 6F - 2), 0);
	}
	default TransformationInstructions getGlovesTransformation(AppearanceFeatureHelper helper) {
		// Applies to gloves from mods, such as from The Aether.
		return this.getPauldronTransformation(helper).flip(this.getArmSize(), 12);
	}
	default TransformationInstructions getUnknownArmsFeatureTransformation(AppearanceFeatureHelper helper) {
		return helper.getStretchingTransformation(this.getArmSize(), new Vector3i(4, 12, 4));
	}
	default TransformationInstructions getBootsTransformation(AppearanceFeatureHelper helper) {
		return helper.getArmorTransformation(
				this.getLegSize(), new Vector3i(4, 12, 4), 2, 0.3F)
				.flip(this.getLegSize(), 12);
	}
	default TransformationInstructions getChaussesTransformation(AppearanceFeatureHelper helper) {
		// Chausses are the part of the leggings that guards the legs.
		return helper.getArmorTransformation(this.getLegSize(), new Vector3i(4, 12, 4), 2, 0.25F);
	}
	default TransformationInstructions getUnknownLegsFeatureTransformation(AppearanceFeatureHelper helper) {
		return helper.getStretchingTransformation(this.getLegSize(), new Vector3i(4, 12, 4));
	}

	default ModelData getModelData(AppearanceGeometryHelper helper) {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		this.makeHead(root, helper);
		this.makeTorso(root, helper);

		this.makeArm(root, helper, false);
		this.makeArm(root, helper, true);

		this.makeLeg(root, helper, false);
		this.makeLeg(root, helper, true);

		return modelData;
	}
}
