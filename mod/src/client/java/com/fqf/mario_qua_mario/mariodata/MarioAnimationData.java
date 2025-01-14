package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.mariodata.util.ArrangementSet;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class MarioAnimationData {
	public @NotNull ArrangementSet prevTickArrangements = new ArrangementSet(); // used for per-frame interpolation
	public @NotNull ArrangementSet thisTickArrangements = new ArrangementSet(); // used for per-frame interpolation
	public @NotNull ArrangementSet prevFrameAnimationDeltas = new ArrangementSet(); // used to undo the previous frame's animation at the start of the next frame (for mod compatibility)
	public boolean animatedLastFrame;
	public int animationTicks;
	public boolean animationMirrored;
	public boolean trailingFrame;

	public static float lerpRadians(float delta, float start, float end) {
		return MathHelper.RADIANS_PER_DEGREE * MathHelper.lerpAngleDegrees(delta, MathHelper.DEGREES_PER_RADIAN * start, MathHelper.DEGREES_PER_RADIAN * end);
	}
}
