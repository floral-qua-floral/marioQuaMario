package com.fqf.charapoweract_api.definitions.states.actions.util.animation;

import com.fqf.charapoweract_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charapoweract_api.cpadata.ICPAReadableMotionData;
import com.fqf.charapoweract_api.util.Easing;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AnimationHelper {
	float interpolateKeyframes(float progress, float first, float second, float... more);

	float easeKeyframes(float progress, float start, List<Pair<Float, Easing>> keyframes);

	float sequencedEase(float progress, Easing first, Easing second, Easing... more);

	@Nullable WallboundActionDefinition.WallInfo getWallInfo(ICPAReadableMotionData data);
}
