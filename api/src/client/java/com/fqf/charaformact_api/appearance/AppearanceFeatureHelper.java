package com.fqf.charaformact_api.appearance;

import org.joml.Vector3f;
import org.joml.Vector3i;

public interface AppearanceFeatureHelper {
	TransformationInstructions getArmorTransformation(Vector3i cuboid, Vector3i vanillaCuboid, int allowance, float overhangPercentage);

	TransformationInstructions getStretchingTransformation(Vector3i cuboid, Vector3i vanillaCuboid);
	TransformationInstructions getStretchingTransformation(Vector3f cuboid, Vector3i vanillaCuboid);
}
