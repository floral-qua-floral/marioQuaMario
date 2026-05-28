package com.fqf.charaformact_api.appearance;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public interface AppearanceGeometryHelper {
	String RIGHT_PANTS = "right_pants";
	String LEFT_PANTS = "left_pants";
	String RIGHT_SLEEVE = "right_sleeve";
	String LEFT_SLEEVE = "left_sleeve";
	String CAPE = "cloak";

	String TAIL = EntityModelPartNames.TAIL;
	String RIGHT_WING = EntityModelPartNames.RIGHT_WING;
	String LEFT_WING = EntityModelPartNames.LEFT_WING;

	ModelPartData makePartAndHat(
			ModelPartData root, boolean isLeft,
			String name, String hatName,
			Vector3f pivot,
			Vector3f offset, float mirrorableXOffset,
			Vector3f rotation,
			Vector3i size,
			Vector2i uv, Vector2i hatUV,
			boolean isVanillaPart
	);

	ModelPartData makePart(
			ModelPartData root, String name, boolean isLeft,
			Vector3f pivot, Vector3f offset, float mirrorableXOffset,
			Vector3f rotation, Vector3i size, Vector2i uv
	);

	ModelPartData makePart(
			ModelPartData root, String name, boolean isLeft,
			Vector3f pivot, Vector3f offset, float mirrorableXOffset,
			Vector3f rotation, Vector3i size, Vector2i uv, float dilation
	);

	Vector2i getUVDimensions(Vector3i cuboidSize);

	Vector2i getBottomRightCorner(Vector2i uv, Vector3i cuboidSize);

	ModelPartData makeInvisiblePart(ModelPartData root, String name, Vector3f pivot, boolean isLeft);

	Vector3f toRadians(float pitch, float yaw, float roll);
	Vector3f toRadians(Vector3f degrees);
}
