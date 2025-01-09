package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AnimationHelper {
	Arrangement.Mutator mutatorFromKeyframes(boolean additivePos, boolean additiveAngles, Arrangement initial,
											 Pair<Easing, Arrangement>[] keyframes);
}
