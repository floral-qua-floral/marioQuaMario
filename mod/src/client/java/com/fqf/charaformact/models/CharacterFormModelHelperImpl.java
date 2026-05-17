package com.fqf.charaformact.models;

import com.fqf.charaformact_api.model.CharacterFormModelHelper;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class CharacterFormModelHelperImpl implements CharacterFormModelHelper {
	public static final CharacterFormModelHelper INSTANCE = new CharacterFormModelHelperImpl();
	private CharacterFormModelHelperImpl() {

	}

	@Override
	public ModelPartData makePartAndHat(
			ModelPartData root, boolean isLeft,
			String name, String hatName,
			Vector3f pivot,
			Vector3f offset, float mirrorableXOffset,
			Vector3i size,
			Vector2i uv, Vector2i hatUV,
			boolean isVanillaPart
	) {
		ModelPartData mainPart = this.makePart(root, name, isLeft, pivot, offset, mirrorableXOffset, size, uv);
		if(isVanillaPart) // Attach the hat-like part to the root with the main part's offsets & pivot. Vanilla code will make it mirror the main part.
			this.makePart(root, hatName, isLeft, pivot, offset, mirrorableXOffset, size, hatUV, 0.5F);
		else // Attach the hat-like layer directly to the main part with no offset or pivot
			this.makePart(mainPart, hatName, isLeft, new Vector3f(), new Vector3f(), mirrorableXOffset, size, hatUV, 0.5F);
		return mainPart;
	}

	@Override
	public ModelPartData makePart(
			ModelPartData root, String name, boolean isLeft,
			Vector3f pivot, Vector3f offset, float mirrorableXOffset,
			Vector3i size, Vector2i uv
	) {
		return this.makePart(root, name, isLeft, pivot, offset, mirrorableXOffset, size, uv, 0);
	}

	@Override
	public ModelPartData makePart(
			ModelPartData root, String name, boolean isLeft,
			Vector3f pivot, Vector3f offset, float mirrorableXOffset,
			Vector3i size, Vector2i uv, float dilation
	) {
		int factor = isLeft ? -1 : 1;
		return root.addChild(
				name,
				ModelPartBuilder.create()
						.uv(uv.x, uv.y)
						.mirrored(isLeft)
						.cuboid(offset.x + mirrorableXOffset * factor, offset.y, offset.z, size.x, size.y, size.z,
								new Dilation(dilation)),
				ModelTransform.pivot(pivot.x * factor, pivot.y, pivot.z)
		);
	}

	@Override
	public Vector2i getBottomRightCorner(Vector2i uv, Vector3i cuboidSize) {
		return new Vector2i(
				uv.x + cuboidSize.x * 2 + cuboidSize.z * 2,
				uv.y + cuboidSize.y + cuboidSize.z
		);
	}

	@Override
	public ModelPartData makeInvisiblePart(ModelPartData root, String name, Vector3f pivot, boolean isLeft) {
		return root.addChild(
				name,
				ModelPartBuilder.create(),
				ModelTransform.pivot(pivot.x * (isLeft ? -1 : 1), pivot.y, pivot.z)
		);
	}
}
