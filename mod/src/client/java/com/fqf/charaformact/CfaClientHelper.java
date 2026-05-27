package com.fqf.charaformact;

import com.fqf.charaformact.cfadata.util.AdvancedPosture;
import com.fqf.charaformact.util.CfaClientHelperManager;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Posture;

import java.util.function.Consumer;

public class CfaClientHelper implements CfaClientHelperManager.ClientHelper {
	@Override
	public void mirrorAndAnimate(Posture posture, Arrangement part, Consumer<Arrangement> animator) {
		mirrorPosture(posture);
		animator.accept(part);
		mirrorPosture(posture);
	}

	@Override
	public void mirrorAndAnimate(Posture posture, Arrangement part, AnimationHelper.SemiSymmetricalAnimator animator) {
		mirrorPosture(posture);
		animator.animate(part, true, 1);
		mirrorPosture(posture);
	}

	private static void mirrorPosture(Posture posture) {
		((AdvancedPosture) posture).fullyMirror();
	}
}
