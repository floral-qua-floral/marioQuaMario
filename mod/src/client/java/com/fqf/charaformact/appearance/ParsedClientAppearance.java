package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact.util.TransformationContext;
import com.fqf.charaformact_api.appearance.*;
import com.fqf.charaformact.util.VanillaPart;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ParsedClientAppearance extends ParsedCommonAppearance {
	private final ClientAppearanceDefinition DEFINITION;

	public final EntityModelLayer LAYER;
	public final Identifier TEXTURE_LOCATION;
	public final int TEXTURE_WIDTH, TEXTURE_HEIGHT;

	private AppearanceModel model;

	public final float HELD_ITEM_X_TRANSLATION, HELD_ITEM_Y_TRANSLATION, HELD_ITEM_Z_TRANSLATION;
	public final float HELD_SHIELD_X_TRANSLATION, HELD_SHIELD_Y_TRANSLATION, HELD_SHIELD_Z_TRANSLATION;
	public final float SHOULDER_PARROT_X_TRANSLATION, SHOULDER_PARROT_Y_TRANSLATION, SHOULDER_PARROT_Z_TRANSLATION;

	public final TransformationInstructions FP_EMPTY_HAND_TRANSFORMATION;
	public final TransformationInstructions FP_FILLED_HAND_TRANSFORMATION;

	public final float LIMB_SWING_MULTIPLIER;
	public final float VIEW_BOB_MULTIPLIER;

	public final Map<VanillaPart, Map<TransformationContext, TransformationInstructions>> FEATURE_TRANSFORMATION_INSTRUCTIONS;

	private List<FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>> customFeatures;

	public ParsedClientAppearance(ClientAppearanceDefinition definition, ParsedCharacter character, ParsedForm form) {
		super(definition, character, form);
		DEFINITION = definition;

		this.LAYER = definition.getModelLayer();
		this.TEXTURE_LOCATION = definition.getTextureLocation();
		Vector2i textureSize = definition.getTextureSize();
		this.TEXTURE_WIDTH = textureSize.x;
		this.TEXTURE_HEIGHT = textureSize.y;

		// ClientAppearanceDefinition's held item offsets are at a different orientation than vanilla uses in its held
		// item render logic. Account for that here.
		Vector3f itemOffset = definition.getHeldItemPosition().div(16);
		this.HELD_ITEM_X_TRANSLATION = itemOffset.x;
		this.HELD_ITEM_Y_TRANSLATION = itemOffset.z; // swapped with Z
		this.HELD_ITEM_Z_TRANSLATION = itemOffset.y; // swapped with Y
		Vector3f shieldOffset = definition.getHeldShieldPosition().div(16);
		this.HELD_SHIELD_X_TRANSLATION = shieldOffset.x;
		this.HELD_SHIELD_Y_TRANSLATION = shieldOffset.z; // swapped with Z
		this.HELD_SHIELD_Z_TRANSLATION = shieldOffset.y; // swapped with Y
		Vector3f shoulderParrotOffset = definition.getShoulderParrotPosition().div(16);
		this.SHOULDER_PARROT_X_TRANSLATION = shoulderParrotOffset.x;
		this.SHOULDER_PARROT_Y_TRANSLATION = shoulderParrotOffset.y;
		this.SHOULDER_PARROT_Z_TRANSLATION = shoulderParrotOffset.z;

		this.FP_EMPTY_HAND_TRANSFORMATION = fixInstructions(definition.getEmptyFpHandTransformation());
		this.FP_FILLED_HAND_TRANSFORMATION = fixInstructions(definition.getFpHandWithMapTransformation());

		AppearanceHelperImpl helper = AppearanceHelperImpl.INSTANCE;

		this.FEATURE_TRANSFORMATION_INSTRUCTIONS = new EnumMap<>(VanillaPart.class);
		this.populateTransformationInstructions(VanillaPart.HEAD,
				definition.getHelmetTransformation(helper), null,
				definition.getHatTransformation(helper), definition.getUnknownHeadFeatureTransformation(helper)
		);
		this.populateTransformationInstructions(VanillaPart.TORSO,
				definition.getCuirassTransformation(helper), definition.getFauldTransformation(helper),
				definition.getBackEquipmentTransformation(helper), definition.getUnknownChestFeatureTransformation(helper)
		);
		this.populateTransformationInstructions(VanillaPart.RIGHT_ARM,
				definition.getPauldronTransformation(helper), null,
				definition.getGlovesTransformation(helper), definition.getUnknownArmsFeatureTransformation(helper)
		);
		this.mirrorTransformationInstructions(VanillaPart.RIGHT_ARM, VanillaPart.LEFT_ARM);
		this.populateTransformationInstructions(VanillaPart.RIGHT_LEG,
				definition.getBootsTransformation(helper), definition.getChaussesTransformation(helper),
				null, definition.getUnknownLegsFeatureTransformation(helper)
		);
		this.mirrorTransformationInstructions(VanillaPart.RIGHT_LEG, VanillaPart.LEFT_LEG);

		this.LIMB_SWING_MULTIPLIER = 1 / this.STRIDE_LENGTH;
		this.VIEW_BOB_MULTIPLIER = this.LIMB_SWING_MULTIPLIER;
	}

	public AppearanceModel makeAndGetModel(EntityRendererFactory.Context ctx) {
		if(this.model == null) this.model = this.DEFINITION.createModel(ctx.getPart(this.LAYER));
		return this.getModel();
	}

	public AppearanceModel getModel() {
		return this.model;
	}

	// good god i cannot BELIEVE i can just get away with all these ugly nasty casts??? the IDE hates it so much :(
	@SuppressWarnings("unchecked")
	public List<FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>> makeCustomFeatures(
			AppearanceRenderer renderer, EntityRendererFactory.Context ctx
	) {
		if(this.customFeatures == null) {
			ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder = ImmutableList.builder();
			this.DEFINITION.accumulateCustomFeatureRenderers(builder, (FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel>) (Object) renderer, ctx);
			this.customFeatures = (List<FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>>) (Object) builder.build();
			for(FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> customFeature : this.customFeatures) {
				// oh my god we do NOT WANT TO TRANSFORM THESE!
				((FeatureRendererWithContext) customFeature).cfa$setContext(TransformationContext.ORIGINAL);
			}
		}
		CharaFormAct.LOGGER.info("Got custom features for {}!\n\t{}", this.ID, this.customFeatures);
		return this.customFeatures;
	}

	private void populateTransformationInstructions(
			VanillaPart part,
			@NotNull TransformationInstructions outerArmor, @Nullable TransformationInstructions innerArmor,
			@Nullable TransformationInstructions special, @NotNull TransformationInstructions unknown
	) {
		EnumMap<TransformationContext, TransformationInstructions> map = new EnumMap<>(TransformationContext.class);
		this.FEATURE_TRANSFORMATION_INSTRUCTIONS.put(part, map);
		map.put(TransformationContext.ARMOR_OUTER, fixArmorInstructions(outerArmor, part, 1));
		if(innerArmor != null) map.put(TransformationContext.ARMOR_INNER, fixArmorInstructions(innerArmor, part, 0.5F));
		if(special != null) map.put(TransformationContext.SPECIAL, fixInstructions(special));
		map.put(TransformationContext.UNKNOWN, fixInstructions(unknown));
	}

	private static Vector3f getVanillaPartSize(VanillaPart part) {
		return switch(part) {
			case HEAD -> new Vector3f(8);
			case TORSO -> new Vector3f(8, 12, 4);
			case RIGHT_ARM, RIGHT_LEG -> new Vector3f(4, 12, 4);
			case LEFT_ARM, LEFT_LEG -> throw new IllegalStateException("Don't use this to get information about a mirrored limb!!");
		};
	}

	private static TransformationInstructions fixArmorInstructions(TransformationInstructions instructions, VanillaPart part, float inflation) {
		TransformationInstructions basicFix = fixInstructions(instructions);

		float addend = inflation * 2;

		Vector3f vanillaCuboidSize = getVanillaPartSize(part);
		Vector3f vanillaArmorSize = new Vector3f(vanillaCuboidSize).add(addend, addend, addend);

		Vector3f modelCuboidSize = new Vector3f(vanillaCuboidSize).mul(basicFix.xScale(), basicFix.yScale(), basicFix.zScale());
		Vector3f modelArmorSize = new Vector3f(modelCuboidSize).add(addend, addend, addend);

		Vector3f newScale = new Vector3f(basicFix.xScale(), basicFix.yScale(), basicFix.zScale())
				.mul(vanillaCuboidSize).div(vanillaArmorSize).mul(modelArmorSize).div(modelCuboidSize);

		return basicFix.withScale(newScale.x, newScale.y, newScale.z);
	}

	private static TransformationInstructions fixInstructions(TransformationInstructions instructions) {
		return instructions.withPos(-instructions.forwards(), -instructions.upwards(), -instructions.rightwards());
	}

	private void mirrorTransformationInstructions(VanillaPart right, VanillaPart left) {
		EnumMap<TransformationContext, TransformationInstructions> leftInstructions = new EnumMap<>(TransformationContext.class);
		this.FEATURE_TRANSFORMATION_INSTRUCTIONS.put(left, leftInstructions);
		this.FEATURE_TRANSFORMATION_INSTRUCTIONS.get(right).forEach((context, instructions) -> {
			CharaFormAct.LOGGER.info("Mirroring transformation instructions for {}'s {}...", right, context);
			if(instructions != null) leftInstructions.put(context, instructions
					.withPos(instructions.forwards(), instructions.upwards(), -instructions.rightwards())
					.withAngles(instructions.pitch(), -instructions.yaw(), -instructions.roll()));
		});
	}
}
