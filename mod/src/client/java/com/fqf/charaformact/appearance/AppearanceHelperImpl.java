package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.appearance.AppearanceFeatureHelper;
import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.TransformationInstructions;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class AppearanceHelperImpl implements AppearanceGeometryHelper, AppearanceFeatureHelper {
	public static final AppearanceHelperImpl INSTANCE = new AppearanceHelperImpl();
	private AppearanceHelperImpl() {

	}

	@Override
	public ModelPartData makePartAndHat(
			ModelPartData root, boolean isLeft,
			String name, String hatName,
			Vector3f pivot,
			Vector3f offset, float mirrorableXOffset,
			Vector3f rotation,
			Vector3i size,
			Vector2i uv, Vector2i hatUV,
			boolean isVanillaPart
	) {
		ModelPartData mainPart = this.makePart(root, name, isLeft, pivot, offset, mirrorableXOffset, new Vector3f(), size, uv);
		if(isVanillaPart) // Attach the hat-like part to the root with the main part's offsets & pivot. Vanilla code will make it mirrorChanges the main part.
			this.makePart(root, hatName, isLeft, pivot, offset, mirrorableXOffset, rotation, size, hatUV, 0.25F);
		else // Attach the hat-like layerPostureMutator directly to the main part with no offset or pivot
			this.makePart(mainPart, hatName, isLeft, new Vector3f(), offset, mirrorableXOffset, new Vector3f(), size, hatUV, 0.25F);
		return mainPart;
	}

	@Override
	public ModelPartData makePart(
			ModelPartData root, String name, boolean isLeft,
			Vector3f pivot, Vector3f offset, float mirrorableXOffset,
			Vector3f rotation, Vector3i size, Vector2i uv
	) {
		return this.makePart(root, name, isLeft, pivot, offset, mirrorableXOffset, rotation, size, uv, 0);
	}

	@Override
	public ModelPartData makePart(
			ModelPartData root, String name, boolean isLeft,
			Vector3f pivot, Vector3f offset, float mirrorableXOffset,
			Vector3f rotation, Vector3i size, Vector2i uv, float dilation
	) {
		int factor = isLeft ? -1 : 1;
		return root.addChild(
				name,
				ModelPartBuilder.create()
						.uv(uv.x, uv.y)
						.mirrored(isLeft)
						.cuboid(offset.x + mirrorableXOffset * factor, offset.y, offset.z, size.x, size.y, size.z,
								new Dilation(dilation)),
				ModelTransform.of(pivot.x * factor, pivot.y, pivot.z, rotation.x, factor * rotation.y, factor * rotation.z)
		);
	}

	@Override
	public Vector2i getUVDimensions(Vector3i cuboidSize) {
		return new Vector2i(cuboidSize.x * 2 + cuboidSize.z * 2, cuboidSize.y + cuboidSize.z);
	}

	@Override
	public Vector2i getBottomRightCorner(Vector2i uv, Vector3i cuboidSize) {
		return new Vector2i(uv).add(this.getUVDimensions(cuboidSize));
	}

	@Override
	public ModelPartData makeInvisiblePart(ModelPartData root, String name, Vector3f pivot, boolean isLeft) {
		return root.addChild(
				name,
				ModelPartBuilder.create(),
				ModelTransform.pivot(pivot.x * (isLeft ? -1 : 1), pivot.y, pivot.z)
		);
	}

	@Override public Vector3f toRadians(float pitch, float yaw, float roll) {
		return new Vector3f(pitch, yaw, roll).mul(MathHelper.RADIANS_PER_DEGREE);
	}
	@Override public Vector3f toRadians(Vector3f degrees) {
		return this.toRadians(degrees.x, degrees.y, degrees.z);
	}

	public TransformationInstructions getArmorTransformation(Vector3i cuboid, Vector3i vanillaCuboid, int allowance, float overhangPercentage) {
		Vector3f scale = new Vector3f(cuboid).div(vanillaCuboid.x, vanillaCuboid.y, vanillaCuboid.z);
		float vanillaCuboidHeight = vanillaCuboid.y * (overhangPercentage + 1);

		// If part is just barely too small for vanilla armor, use vanilla armor size anyways
		if(scale.x < 1 && scale.x >= 1 - (float) allowance / vanillaCuboid.x) {
			if (scale.z < 1 && scale.z >= 1 - (float) allowance / vanillaCuboid.z) {
				scale.x = 1;
				scale.z = 1;
			}
		}
		if(scale.y < 1 && scale.y >= 1 - (float) allowance / vanillaCuboidHeight)
			scale.y = 1;

		// If part is just barely too small for maintained horizontal aspect ratio, maintain horizontal aspect ratio anyways
		if(scale.x < scale.z && scale.x >= scale.z - (float) allowance / vanillaCuboid.x) {
			scale.x = scale.z;
		}
		if(scale.z < scale.x && scale.z >= scale.x - (float) allowance / vanillaCuboid.z) {
			scale.z = scale.x;
		}

		// If part is tall enough to support the Y scale matching a horizontal scale, then do that. Prefer matching X.
		if(cuboid.y * (1 + overhangPercentage) >= scale.x * vanillaCuboid.y)
			//noinspection SuspiciousNameCombination
			scale.y = scale.x;
		else if(cuboid.y * (1 + overhangPercentage) >= scale.z * vanillaCuboid.y)
			scale.y = scale.z;

		// Return the new transformation
		return new TransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				scale.x, scale.y, scale.z
		);
	}

	public TransformationInstructions getStretchingTransformation(Vector3i cuboid, Vector3i vanillaCuboid) {
		var uwu = new TransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				(float) cuboid.x / vanillaCuboid.x,
				(float) cuboid.y / vanillaCuboid.y,
				(float) cuboid.z / vanillaCuboid.z
		);
		CharaFormAct.LOGGER.info("STRETCHING TRANSFORMATION TIME!!!!\n\tVanilla part: {}, {}, {}\n\tCustom: {}, {}, {}\n\tResult: {}",
				vanillaCuboid.x, vanillaCuboid.y, vanillaCuboid.z, cuboid.x, cuboid.y, cuboid.z, uwu);
		return uwu;
	}
}
