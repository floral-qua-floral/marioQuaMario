package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact.util.TransformationContext;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.charaformact_api.appearance.FeatureTransformationInstructions;
import com.fqf.charaformact.util.VanillaPart;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.EnumMap;
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

	public final float LIMB_SWING_MULTIPLIER;
	public final float VIEW_BOB_MULTIPLIER;

	public final Map<VanillaPart, Map<TransformationContext, FeatureTransformationInstructions>> FEATURE_TRANSFORMATION_INSTRUCTIONS;

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

		this.FEATURE_TRANSFORMATION_INSTRUCTIONS = new EnumMap<>(VanillaPart.class);
		this.populateTransformationInstructions(VanillaPart.HEAD,
				definition.getHelmetTransformation(), null,
				definition.getHatTransformation(), definition.getUnknownHeadFeatureTransformation()
		);
		this.populateTransformationInstructions(VanillaPart.TORSO,
				definition.getCuirassTransformation(), definition.getFauldTransformation(),
				definition.getBackEquipmentTransformation(), definition.getUnknownChestFeatureTransformation()
		);
		this.populateTransformationInstructions(VanillaPart.RIGHT_ARM,
				definition.getPauldronTransformation(), null,
				definition.getGlovesTransformation(), definition.getUnknownArmsFeatureTransformation()
		);
		this.mirrorTransformationInstructions(VanillaPart.RIGHT_ARM, VanillaPart.LEFT_ARM);
		this.populateTransformationInstructions(VanillaPart.RIGHT_LEG,
				definition.getBootsTransformation(), definition.getChaussesTransformation(),
				null, definition.getUnknownLegsFeatureTransformation()
		);
		this.mirrorTransformationInstructions(VanillaPart.RIGHT_LEG, VanillaPart.LEFT_LEG);

		this.LIMB_SWING_MULTIPLIER = 1 / this.STRIDE_LENGTH;
		this.VIEW_BOB_MULTIPLIER = Math.min(1, this.LIMB_SWING_MULTIPLIER);
	}

	public AppearanceModel makeAndGetModel(EntityRendererFactory.Context ctx) {
		if(this.model == null) this.model = this.DEFINITION.createModel(ctx.getPart(this.LAYER));
		return this.getModel();
	}

	public AppearanceModel getModel() {
		return this.model;
	}

	private void populateTransformationInstructions(
			VanillaPart part,
			@NotNull FeatureTransformationInstructions outerArmor, @Nullable FeatureTransformationInstructions innerArmor,
			@Nullable FeatureTransformationInstructions special, @NotNull FeatureTransformationInstructions unknown
	) {
		EnumMap<TransformationContext, FeatureTransformationInstructions> map = new EnumMap<>(TransformationContext.class);
		this.FEATURE_TRANSFORMATION_INSTRUCTIONS.put(part, map);
		map.put(TransformationContext.ARMOR_OUTER, fixInstructions(outerArmor));
		if(innerArmor != null) map.put(TransformationContext.ARMOR_INNER, fixInstructions(innerArmor));
		if(special != null) map.put(TransformationContext.SPECIAL, fixInstructions(special));
		map.put(TransformationContext.UNKNOWN, fixInstructions(unknown));
	}

	private static FeatureTransformationInstructions fixInstructions(FeatureTransformationInstructions instructions) {
		return new FeatureTransformationInstructions(
				instructions.forwards() * -1,
				instructions.upwards() * -1,
				instructions.rightwards() * -1,
				instructions.pitch(), instructions.yaw(), instructions.roll(),
				instructions.xScale(), instructions.yScale(), instructions.zScale()
		);
	}

	private void mirrorTransformationInstructions(VanillaPart right, VanillaPart left) {
		EnumMap<TransformationContext, FeatureTransformationInstructions> leftInstructions = new EnumMap<>(TransformationContext.class);
		this.FEATURE_TRANSFORMATION_INSTRUCTIONS.put(left, leftInstructions);
		this.FEATURE_TRANSFORMATION_INSTRUCTIONS.get(right).forEach((context, instructions) -> {
			CharaFormAct.LOGGER.info("Mirroring transformation instructions for {}'s {}...", right, context);
			if(instructions != null) leftInstructions.put(context, new FeatureTransformationInstructions(
					instructions.forwards(), instructions.upwards(), -instructions.rightwards(),
					instructions.pitch(), -instructions.yaw(), -instructions.roll(),
					instructions.xScale(), instructions.yScale(), instructions.zScale()
			));
		});
	}
}
