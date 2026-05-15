package com.fqf.charaformact_api.model;

import net.minecraft.client.model.*;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public interface CharacterFormModelHelper {
	String RIGHT_PANTS = "right_pants";
	String LEFT_PANTS = "left_pants";
	String RIGHT_SLEEVE = "right_sleeve";
	String LEFT_SLEEVE = "left_sleeve";
	String CAPE = "cloak";
	String TAIL = "tail";

	default ModelPartData makePartAndHat(
			ModelPartData root, boolean isLeft,
			String name, String hatName,
			Vector3f pivot,
			Vector3f offset, float mirrorableXOffset,
			Vector3i size,
			Vector2i uv, Vector2i hatUV,
			boolean isVanillaPart
	) {
//		pivot = new Vector3f();
//		offset = new Vector3f();

		ModelPartData mainPart = this.makePart(root, name, isLeft, pivot, offset, mirrorableXOffset, size, uv);
		if(isVanillaPart) // Attach the hat-like part to the root with the main part's offsets & pivot. Vanilla code will make it mirror the main part.
			this.makePart(root, hatName, isLeft, pivot, offset, mirrorableXOffset, size, hatUV, 0.5F);
		else // Attach the hat-like layer directly to the main part with no offset or pivot
			this.makePart(mainPart, hatName, isLeft, new Vector3f(), new Vector3f(), mirrorableXOffset, size, hatUV, 0.5F);
		return mainPart;
	}

	default ModelPartData makePart(
			ModelPartData root, String name, boolean isLeft,
			Vector3f pivot, Vector3f offset, float mirrorableXOffset,
			Vector3i size, Vector2i uv
	) {
		return this.makePart(root, name, isLeft, pivot, offset, mirrorableXOffset, size, uv, 0);
	}

	default ModelPartData makePart(
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

	default Vector2i getBottomRightCorner(Vector2i uv, Vector3i cuboidSize) {
		return new Vector2i(
				uv.x + cuboidSize.x * 2 + cuboidSize.z * 2,
				uv.y + cuboidSize.y + cuboidSize.z
		);
	}

	default void addUnusedEasterEggEarThatYouWillNeverSee(ModelPartData root) {
		root.addChild("ear", ModelPartBuilder.create(), ModelTransform.NONE);
	}

	default ModelPartData getSmartOffsetRoot(ModelData modelData) {
		return modelData.getRoot().addChild(
				"smart_offset",
				ModelPartBuilder.create(),
				ModelTransform.pivot(0, -10, 0)
		);
	}
}
