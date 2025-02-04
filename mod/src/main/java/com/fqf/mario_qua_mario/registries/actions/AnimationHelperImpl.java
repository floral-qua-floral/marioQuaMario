package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

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
	public float easeKeyframes(float progress, float start, List<Pair<Float, Easing>> keyframes) {
		if(progress < 0) return start;

		List<Pair<Float, Easing>> all_keyframes = new ArrayList<>(keyframes.size() + 1);
		all_keyframes.add(new Pair<>(start, null));
		all_keyframes.addAll(keyframes);
		int starting_index = MathHelper.floor(progress);
		int ending_index = starting_index + 1;
		if(ending_index >= all_keyframes.size()) return all_keyframes.getLast().getLeft();
		return all_keyframes.get(ending_index).getRight().ease(progress % 1, all_keyframes.get(starting_index).getLeft(), all_keyframes.get(ending_index).getLeft());
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
//		MarioQuaMario.LOGGER.info("Sequenced Ease:\nProgress: {}\nEasing Index: {}\nPre-post:\n{}\n{}",
//				progress, easingIndex, progress % 1, useEase.ease(progress % 1));
		return easingIndex + useEase.ease(progress % 1);
	}
}
