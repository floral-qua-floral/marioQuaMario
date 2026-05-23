package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Posture;
import com.fqf.charaformact_api.util.Easing;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.util.math.MathHelper.lerp;

public class PostureHelper {
	public static Posture fromModel(PlayerEntityModel<?> model) {
		ModelPart tail, rightEar, leftEar;
		if(model instanceof AppearanceModel appearanceModel) {
			tail = appearanceModel.tail; rightEar = appearanceModel.rightEar; leftEar = appearanceModel.leftEar;
		}
		else {
			tail = null; rightEar = null; leftEar = null;
		}
		return new Posture(new Arrangement[]{
				new AdvancedArrangement(), // everything
				AdvancedArrangement.of(model.head),
				AdvancedArrangement.of(model.body),
				AdvancedArrangement.of(model.rightArm),
				AdvancedArrangement.of(model.leftArm),
				AdvancedArrangement.of(model.rightLeg),
				AdvancedArrangement.of(model.leftLeg),
				AdvancedArrangement.of(tail),
				AdvancedArrangement.of(rightEar),
				AdvancedArrangement.of(leftEar)
		});
	}

	public static Posture copy(Posture posture) {
		return new Posture(new Arrangement[]{
				posture.EVERYTHING, posture.HEAD, posture.TORSO,
				posture.RIGHT_ARM, posture.LEFT_ARM,
				posture.RIGHT_LEG, posture.LEFT_LEG,
				posture.TAIL, posture.RIGHT_EAR, posture.LEFT_EAR
		});
	}

	public static void interpolatePosture(Posture mutate, Posture from, Posture to, float progress) {
		interpolateArrangement(mutate.EVERYTHING, from.EVERYTHING, to.EVERYTHING, progress);
		interpolateArrangement(mutate.HEAD, from.HEAD, to.HEAD, progress);
		interpolateArrangement(mutate.TORSO, from.TORSO, to.TORSO, progress);
		interpolateArrangement(mutate.RIGHT_ARM, from.RIGHT_ARM, to.RIGHT_ARM, progress);
		interpolateArrangement(mutate.LEFT_ARM, from.LEFT_ARM, to.LEFT_ARM, progress);
		interpolateArrangement(mutate.RIGHT_LEG, from.RIGHT_LEG, to.RIGHT_LEG, progress);
		interpolateArrangement(mutate.LEFT_LEG, from.LEFT_LEG, to.LEFT_LEG, progress);
		interpolateArrangement(mutate.TAIL, from.TAIL, to.TAIL, progress);
		interpolateArrangement(mutate.RIGHT_EAR, from.RIGHT_EAR, to.RIGHT_EAR, progress);
		interpolateArrangement(mutate.LEFT_EAR, from.LEFT_EAR, to.LEFT_EAR, progress);
	}

	private static void interpolateArrangement(Arrangement mutate, Arrangement from, Arrangement to, float progress) {
		if(mutate == null || from == null || to == null) return;
		mutate.setPos(lerp(progress, from.x, to.x), lerp(progress, from.y, to.y), lerp(progress, from.z, to.z));
		mutate.setAngles(lerp(progress, from.pitch, to.pitch), lerp(progress, from.yaw, to.yaw),lerp(progress, from.roll, to.roll));
	}
}
