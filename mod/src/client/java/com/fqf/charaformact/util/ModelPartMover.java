package com.fqf.charaformact.util;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.models.ParsedCharacterFormModel;
import com.fqf.charaformact_api.model.CharacterFormEntityModel;
import com.fqf.charaformact_api.model.FeatureTransformationInstructions;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public class ModelPartMover {
	public static ModelPartMover instance;

	public static Vector3f movePointLocally(ModelPart part, float backwards, float downwards, float leftwards) {
		if(part.roll == 0)
			return movePointLocallyWithoutRoll(part, backwards, downwards, leftwards);
		return movePointLocallyWithRoll(part, backwards, downwards, leftwards);
	}

	private static Vector3f movePointLocallyWithoutRoll(ModelPart part, float backwards, float downwards, float leftwards) {
		float cosPitch = MathHelper.cos(part.pitch);
		float sinPitch = MathHelper.sin(part.pitch);

		float cosYaw = MathHelper.cos(part.yaw);
		float sinYaw = MathHelper.sin(part.yaw);

		return new Vector3f(
				part.pivotX + sinYaw * cosPitch * backwards + cosYaw * leftwards + sinYaw * sinPitch * downwards,
				part.pivotY - sinPitch * backwards + cosPitch * downwards,
				part.pivotZ + cosYaw * cosPitch * backwards - sinYaw * leftwards + cosYaw * sinPitch * downwards
		);
	}
	private static Vector3f movePointLocallyWithRoll(ModelPart part, float backwards, float downwards, float leftwards) {
		float cosPitch = MathHelper.cos(part.pitch);
		float sinPitch = MathHelper.sin(part.pitch);

		float cosYaw = MathHelper.cos(part.yaw);
		float sinYaw = MathHelper.sin(part.yaw);

		float cosRoll = MathHelper.cos(part.roll);
		float sinRoll = MathHelper.sin(part.roll);

		// Forward vector
		float forwardX = sinYaw * cosPitch;
		float forwardY = -sinPitch;
		float forwardZ = cosYaw * cosPitch;

		// Right vector
		float rightX = cosYaw * cosRoll + sinYaw * sinPitch * sinRoll;
		float rightY = cosPitch * sinRoll;
		float rightZ = -sinYaw * cosRoll + cosYaw * sinPitch * sinRoll;

		// Up vector
		float upX = -cosYaw * sinRoll + sinYaw * sinPitch * cosRoll;
		float upY = cosPitch * cosRoll;
		float upZ = sinYaw * sinRoll + cosYaw * sinPitch * cosRoll;

		// Calculate movement
		return new Vector3f(
			part.pivotX + forwardX * backwards + rightX * leftwards + upX * downwards,
			part.pivotY + forwardY * backwards + rightY * leftwards + upY * downwards,
			part.pivotZ + forwardZ * backwards + rightZ * leftwards + upZ * downwards
		);
	}

	private static class UsableTransformation {
		public final float x, y, z;
		public final float pitch, yaw, roll;
		public final float xScale, yScale, zScale;
		public UsableTransformation(ModelPart part) {
			this.x = part.pivotX; this.y = part.pivotY; this.z = part.pivotZ;
			this.pitch = part.pitch; this.yaw = part.yaw; this.roll = part.roll;
			this.xScale = part.xScale; this.yScale = part.yScale; this.zScale = part.zScale;
		}
		public UsableTransformation(ModelPart part, FeatureTransformationInstructions instruction) {
			Vector3f pos = movePointLocally(part, instruction.backwards(), instruction.downwards(), instruction.leftwards());
			this.x = pos.x; this.y = pos.y; this.z = pos.z;
			this.pitch = part.pitch + instruction.pitch();
			this.yaw = part.yaw + instruction.yaw();
			this.roll = part.roll + instruction.roll();
			this.xScale = part.xScale * instruction.xScale();
			this.yScale = part.yScale * instruction.yScale();
			this.zScale = part.zScale * instruction.zScale();
		}
		public void apply(ModelPart part) {
			part.setPivot(this.x, this.y, this.z);
			part.setAngles(this.pitch, this.yaw, this.roll);
			part.xScale = this.xScale; part.yScale = this.yScale; part.zScale = this.zScale;
		}

		@Override
		public String toString() {
			return "UsableTransformation: \n\t" + this.x + ", " + this.y + ", " + this.z + "\n\t" + this.pitch + ", "
					+ this.yaw + ", " + this.roll + "\n\t" + this.xScale + ", " + this.yScale + ", " + this.zScale;
		}
	}

	private final Map<VanillaPart, Map<TransformationContext, UsableTransformation>> TRANSFORMATIONS;
	private final CharacterFormEntityModel ENTITY_MODEL;
	private TransformationContext currentContext = TransformationContext.ORIGINAL;

	public ModelPartMover(ParsedCharacterFormModel parsedModel, CharacterFormEntityModel entityModel) {
		this.ENTITY_MODEL = entityModel;
		this.TRANSFORMATIONS = new EnumMap<>(VanillaPart.class);
		for(VanillaPart vanillaPart : VanillaPart.values()) {
			// First record the original position, orientation, and scale of every single model vanillaPart
			ModelPart modelPart = vanillaPart.of(entityModel);
			Map<TransformationContext, UsableTransformation> transformations = new EnumMap<>(TransformationContext.class);
			this.TRANSFORMATIONS.put(vanillaPart, transformations);
			UsableTransformation original = new UsableTransformation(modelPart);
			transformations.put(TransformationContext.ORIGINAL, original);

			// Calculate the different transformations we'll be using for this part
			Map<TransformationContext, FeatureTransformationInstructions> instructions = parsedModel.FEATURE_TRANSFORMATION_INSTRUCTIONS.get(vanillaPart);
			this.populateTransformationsMap(TransformationContext.ARMOR_OUTER, transformations, modelPart, instructions);
			if(vanillaPart.HAS_INNER_ARMOR)
				this.populateTransformationsMap(TransformationContext.ARMOR_INNER, transformations, modelPart, instructions);
			if(vanillaPart.HAS_SPECIAL)
				this.populateTransformationsMap(TransformationContext.SPECIAL, transformations, modelPart, instructions);
		}
	}

	private void populateTransformationsMap(
			TransformationContext context,
			Map<TransformationContext, UsableTransformation> transformations, ModelPart part,
			Map<TransformationContext, FeatureTransformationInstructions> instructions
	) {
		transformations.put(context, new UsableTransformation(part, instructions.get(context)));
	}

	public void setTo(TransformationContext context) {
		// There's no need to reset everything again if we're already there! Take it easy!
		if(context == this.currentContext) return;
		this.TRANSFORMATIONS.forEach((vanillaPart, elaborateTransformationGroup) -> {
//			CharaFormAct.LOGGER.info("Transforming {} with context {}!\n\tNew status: {}",
//					vanillaPart, context, this.TRANSFORMATIONS.get(vanillaPart).get(context));
			UsableTransformation transformation = this.TRANSFORMATIONS.get(vanillaPart).get(context);
			if(transformation != null) transformation.apply(vanillaPart.of(this.ENTITY_MODEL));
		});
		this.currentContext = context;
	}
}
