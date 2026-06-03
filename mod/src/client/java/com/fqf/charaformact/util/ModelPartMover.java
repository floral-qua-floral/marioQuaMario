package com.fqf.charaformact.util;

import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.appearance.TransformationInstructions;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

/**
 * We transform Render Features by translating, rotating, and scaling the vanilla model parts right before every feature
 * gets rendered. Thus, when it attaches itself to the vanilla part for the frame, it gets transformed the same way.
 * This occurs after the player herself has been rendered, so we're free to just go crazy mucking up the body parts.
 * <p>
 * We don't have any way of knowing which body part a feature is rendering on, so instead we figure out its general
 * nature, and then transform every single body part in a way that matches that nature. For instance, when we're
 * rendering a helmet, we don't actually know it's a helmet, but we do know it's an ARMOR_OUTER. So we just transform
 * every body part to its ARMOR_OUTER transformations, like a sort of shotgun approach. That way no matter what the
 * feature is attaching to, it'll be affected the way we want.
 * <p>
 * We have no control over what order features will be rendered in. So we can't just do all the ARMOR_OUTER features,
 * then all the SPECIAL features, and so on, even though that would be most efficient, since then we'd only need to
 * calculate and apply each complete set of rotations a single time. We can't do that, though - we'll be going back and
 * forth and back and forth many times per frame. So we just take our lumps and cache each full set of transformations
 * for every context so that at least we only have to calculate each one once. I hope this doesn't incur a high
 * performance cost :(
 */
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
		public UsableTransformation(ModelPart part, TransformationInstructions instruction) {
			Vector3f pos = movePointLocally(part, instruction.forwards(), instruction.upwards(), instruction.rightwards());
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
	private final AppearanceModel MODEL;
	private TransformationContext currentContext = TransformationContext.ORIGINAL;

	public ModelPartMover(ParsedClientAppearance appearance, AppearanceModel model) {
		this.MODEL = model;
		this.TRANSFORMATIONS = new EnumMap<>(VanillaPart.class);
		for(VanillaPart vanillaPart : VanillaPart.values()) {
			// First record the original position, orientation, and scale of every single model vanillaPart
			ModelPart modelPart = vanillaPart.of(model);
			Map<TransformationContext, UsableTransformation> transformations = new EnumMap<>(TransformationContext.class);
			this.TRANSFORMATIONS.put(vanillaPart, transformations);
			UsableTransformation original = new UsableTransformation(modelPart);
			transformations.put(TransformationContext.ORIGINAL, original);

			// Calculate the different transformations we'll be using for this part
			Map<TransformationContext, TransformationInstructions> instructions = appearance.FEATURE_TRANSFORMATION_INSTRUCTIONS.get(vanillaPart);
			this.populateTransformationsMap(TransformationContext.ARMOR_OUTER, transformations, modelPart, instructions);
			if(vanillaPart.HAS_INNER_ARMOR)
				this.populateTransformationsMap(TransformationContext.ARMOR_INNER, transformations, modelPart, instructions);
			if(vanillaPart.HAS_SPECIAL)
				this.populateTransformationsMap(TransformationContext.SPECIAL, transformations, modelPart, instructions);
			this.populateTransformationsMap(TransformationContext.UNKNOWN, transformations, modelPart, instructions);
		}
	}

	private void populateTransformationsMap(
			TransformationContext context,
			Map<TransformationContext, UsableTransformation> transformations, ModelPart part,
			Map<TransformationContext, TransformationInstructions> instructions
	) {
		transformations.put(context, new UsableTransformation(part, instructions.get(context)));
	}

	public void setTo(TransformationContext context) {
		// There's no need to reset everything again if we're already there! Take it easy!
		if(context == this.currentContext) return;

		this.TRANSFORMATIONS.forEach((vanillaPart, elaborateTransformationGroup) -> {
			UsableTransformation transformation = this.TRANSFORMATIONS.get(vanillaPart).get(context);
			if(transformation != null) transformation.apply(vanillaPart.of(this.MODEL));
		});
		this.currentContext = context;
	}
}
