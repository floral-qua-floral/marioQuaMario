package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario.util.Easing;
import com.tom.cpl.math.Vec3f;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class AnimationHelperImpl implements AnimationHelper {
	@Override public Arrangement.Mutator mutatorFromKeyframes(
			boolean additivePos, boolean additiveAngles,
			Arrangement initial, Pair<Easing, Arrangement>[] keyframes
	) {
		return null;
	}

	public static float torsoDeltaX, torsoDeltaY, torsoDeltaZ;
	public static float torsoDeltaPitch, torsoDeltaYaw, torsoDeltaRoll;

	@Override public Arrangement.Mutator smartHeadPositioner() {
//		return (data, arrangement, progress, loops) -> {
//			arrangement.addPos(torsoDeltaX, torsoDeltaY, torsoDeltaZ);
//		};
		return null;
	}
	@Override public Arrangement.Mutator smartHeadPositionerWithMutator(Arrangement.Mutator mutator) {
		return null;
	}

	@Override public Arrangement.Mutator smartArmPositioner() {
		return null;
	}
	@Override public Arrangement.Mutator smartArmPositionerWithMutator(Arrangement.Mutator mutator) {
		return null;
	}

	@Override public Arrangement.Mutator smartLegPositioner() {
		return null;
	}
	@Override public Arrangement.Mutator smartLegPositionerWithMutator(Arrangement.Mutator mutator) {
		return null;
	}

	@Override public Arrangement.Mutator smartTailPositioner() {
		return null;
	}
	@Override public Arrangement.Mutator smartTailPositionerWithMutator(Arrangement.Mutator mutator) {
		return null;
	}
}
