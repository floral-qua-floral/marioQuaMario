package com.fqf.mario_qua_mario.appearances.util;

import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class RaccoonUtil {
	public static void addTail(ModelPartData torso, Vector3i torsoSize, Vector2i tailUV, AppearanceGeometryHelper helper) {
		ModelPartData tailPart1 = helper.makePart(
				torso, EntityModelPartNames.TAIL, false,
				new Vector3f(0, torsoSize.y - 1.01F, torsoSize.z / 2F - 1), // pivot
				new Vector3f(-1, -1, 0), 0, // offset
				new Vector3f(), new Vector3i(2, 2, 2), tailUV
		);
		ModelPartData tailPart2 = helper.makePart(
				tailPart1, "tail_2", false,
				new Vector3f(0, 0, 2), // pivot
				new Vector3f(-1.5F, -1.5F, 0), 0, // offset
				new Vector3f(), new Vector3i(3, 3, 2), new Vector2i(tailUV).add(16, 0)
		);
		ModelPartData tailPart3 = helper.makePart(
				tailPart2, "tail_3", false,
				new Vector3f(0, 0, 2), // pivot
				new Vector3f(-2, -2, 0), 0, // offset
				new Vector3f(), new Vector3i(4, 4, 7), new Vector2i(tailUV).add(1, 0)
		);
		helper.makePart(
				tailPart3, "tail_4", false,
				new Vector3f(0, 0, 6.5F), // pivot
				new Vector3f(-1, -1, 0), 0, // offset
				new Vector3f(), new Vector3i(2, 2, 1), new Vector2i(tailUV).add(0, 4)
		);
	}

	public static void addEars(ModelPartData head, Vector3f pivot, Vector2i innerUV1, Vector2i innerUV2, Vector2i outerUV, AppearanceGeometryHelper helper) {
		addEar(head, false, pivot, innerUV1, innerUV2, outerUV, helper);
		addEar(head, true, pivot, innerUV1, innerUV2, outerUV, helper);
	}

	private static void addEar(
			ModelPartData head, boolean isLeft,
			Vector3f pivot,
			Vector2i innerUV1, Vector2i innerUV2, Vector2i outerUV,
			AppearanceGeometryHelper helper
	) {
		ModelPartData earBase = makeEarHalf(
				head, isLeft ? EntityModelPartNames.LEFT_EAR : EntityModelPartNames.RIGHT_EAR, isLeft, false,
				pivot,
//				helper.toRadians(0, -10, 0),
				helper.toRadians(-10, -10, 20),
				innerUV1, outerUV, helper
		);
		makeEarHalf(
				earBase, isLeft ? "left_ear_flap" : "right_ear_flap", isLeft, true,
				new Vector3f(), helper.toRadians(0, 110, 0),
				innerUV2, outerUV, helper
		);
	}

	private static ModelPartData makeEarHalf(
			ModelPartData attachTo, String name, boolean isLeft, boolean isFlap,
			Vector3f pivot, Vector3f rotation,
			Vector2i innerUV, Vector2i outerUV, AppearanceGeometryHelper helper
	) {
		ModelPartData outer = helper.makePart(
				attachTo, name, isLeft,
				pivot, // pivot
				new Vector3f(isLeft ? -3 : 0, -4, 0), // offset
				0, // mirrorable offset
				rotation, new Vector3i(3, 6, 0),
				outerUV
		);
		helper.makePart(
				outer, name + "_inner", isLeft,
				new Vector3f(), // pivot
				new Vector3f(isLeft ? -3 : 0, -4, (isFlap ? 1 : -1) * 0.01F), // offset
				0, // mirrorable offset
				new Vector3f(), new Vector3i(3, 6, 0),
				innerUV
		);
		return outer;
	}

	public static class RaccoonFormEyesFeatureRenderer extends EyesFeatureRenderer<AbstractClientPlayerEntity, AppearanceModel> {
		private final RenderLayer TEXTURE;

		public RaccoonFormEyesFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, Identifier texture) {
			super(featureRendererContext);
			this.TEXTURE = RenderLayer.getEyes(Identifier.of(texture.getNamespace(), texture.getPath().replace(".png", "_eyes.png")));
		}

		@Override
		public RenderLayer getEyesTexture() {
			return this.TEXTURE;
		}
	}
}
