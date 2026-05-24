package com.fqf.charaformact.registries.actions;

import com.fqf.charaformact.util.CfaClientHelperManager;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Posture;
import com.fqf.charaformact_api.util.Easing;
import it.unimi.dsi.fastutil.floats.FloatObjectImmutablePair;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AnimationHelperImpl implements AnimationHelper {
	public static final AnimationHelperImpl INSTANCE = new AnimationHelperImpl();

	@Override
	public float interpolateKeyframes(float progress, float first, float second, float... more) {
		if(progress < 0) return first;

		float[] keyframes = new float[more.length + 2];
		keyframes[0] = first; keyframes[1] = second;
		System.arraycopy(more, 0, keyframes, 2, more.length);
		int starting_index = MathHelper.floor(progress);
		int ending_index = starting_index + 1;
		if(ending_index >= keyframes.length) return keyframes[keyframes.length - 1];
		return MathHelper.lerp(progress % 1, keyframes[starting_index], keyframes[ending_index]);
	}

	@Override
	public float easeKeyframes(float progress, float start, List<FloatObjectImmutablePair<Easing>> keyframes) {
		if(progress < 0) return start;

		List<FloatObjectImmutablePair<Easing>> all_keyframes = new ArrayList<>(keyframes.size() + 1);
		all_keyframes.add(new FloatObjectImmutablePair<>(start, null));
		all_keyframes.addAll(keyframes);
		int starting_index = MathHelper.floor(progress);
		int ending_index = starting_index + 1;
		if(ending_index >= all_keyframes.size()) return all_keyframes.getLast().leftFloat();
		return all_keyframes.get(ending_index).right().ease(progress % 1, all_keyframes.get(starting_index).leftFloat(), all_keyframes.get(ending_index).leftFloat());
	}

	@Override
	public float sequencedEase(float progress, Easing first, Easing second, Easing... more) {
		if(progress < 0) return 0;
		if(progress >= 2 + more.length) return more.length + 2;

		int easingIndex = MathHelper.floor(progress);
		Easing useEase = switch(easingIndex) {
			case 0 -> first;
			case 1 -> second;
			default -> more[easingIndex - 2];
		};
//		CharaFormAct.LOGGER.info("Sequenced Ease:\nProgress: {}\nEasing Index: {}\nPre-post:\n{}\n{}",
//				progress, easingIndex, progress % 1, useEase.ease(progress % 1));
		return easingIndex + useEase.ease(progress % 1);
	}

	@Override
	public @Nullable WallboundActionDefinition.WallInfo getWallInfo(CfaReadableMotionData data) {
		if(data.getActionCategory() != ActionCategory.WALLBOUND) return null;
		return UniversalActionDefinitionHelper.INSTANCE.getWallInfo(data);
	}

	@Override
	public void symmetricallyAnimate(Posture posture, Arrangement rightPart, Consumer<Arrangement> animator) {
		animator.accept(rightPart);
		CfaClientHelperManager.helper.mirrorAndAnimate(posture, rightPart, animator);
	}

	@Override
	public void symmetricallyAnimate(Posture posture, Arrangement rightPart, SymmetricalAnimator animator) {
		animator.animate(rightPart, false);
		CfaClientHelperManager.helper.mirrorAndAnimate(posture, rightPart, animator);
	}

	@Override
	public void multiAnimate(Consumer<Arrangement> animator, Arrangement... parts) {
		for(Arrangement part : parts) {
			animator.accept(part);
		}
	}
}
