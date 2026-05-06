package com.fqf.charaformact_api.cfadata;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface CfaTravelData extends CfaReadableMotionData {
	void setForwardVel(double forward);
	void setStrafeVel(double strafe);
	default void setForwardStrafeVel(double forward, double strafe) {
		this.setForwardVel(forward);
		this.setStrafeVel(strafe);
	}
	void setYVel(double vertical);

	void setVelocity(Vec3d velocity);
	void goTo(Vec3d pos);
	default void centerLaterally() {
		this.goTo(this.getPlayer().getBlockPos().toCenterPos().withAxis(Direction.Axis.Y, this.getPlayer().getY()));
	}

	void approachAngleAndAccel(
			double forwardAccel, double forwardTarget, double strafeAccel, double strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, double redirectDelta
	);

	void refreshJumpCapping();
}
