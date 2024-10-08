package fqf.qua_mario.cameraanims;

import net.minecraft.util.math.MathHelper;

public abstract class CameraAnim {
	@FunctionalInterface
	protected interface MarioStateTransition {
		CameraAnim evaluate();
	}

	public String name;
	public double duration;

	public abstract double[] getRotations(double progress);

	protected double cappedLerp(double delta, double start, double end) {
		return MathHelper.clampedLerp(start, end, delta);
	}
}

