package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.math.MathHelper.PI;
import static net.minecraft.util.math.MathHelper.TAU;

public class AdvancedArrangement extends Arrangement {
	public static final int BEFORE_CFA_ANIMATIONS = 0;

	private final Arrangement[] STORAGE;

	public AdvancedArrangement() {
		this.STORAGE = new Arrangement[3];
	}

	public static @Nullable AdvancedArrangement of(ModelPart part) {
		if(part == null) return null;
		AdvancedArrangement build = new AdvancedArrangement();
		build.setPos(part.pivotX, part.pivotY, part.pivotZ);
		build.setAngles(part.pitch, part.yaw, part.roll);
		return build;
	}

	public void store(int slot) {
		Arrangement storeTo = new Arrangement();
		storeTo.setPos(this.x, this.y, this.z);
		storeTo.setAngles(this.pitch, this.yaw, this.roll);
		this.STORAGE[slot] = storeTo;
	}

	public void resetTo(int slot) {
		Arrangement loadFrom = this.STORAGE[slot];
		this.setPos(loadFrom.x, loadFrom.y, loadFrom.z);
		this.setAngles(loadFrom.pitch, loadFrom.yaw, loadFrom.roll);
	}

	public Arrangement getDeltas(int slot) {
		Arrangement changesFrom = this.STORAGE[slot];
		Arrangement deltas = new Arrangement();
		deltas.setPos(this.x - changesFrom.x, this.y - changesFrom.y, this.z - changesFrom.z);
		deltas.setAngles(this.pitch - changesFrom.pitch, this.yaw - changesFrom.yaw, this.roll - changesFrom.roll);
		return deltas;
	}

	public void mirrorChanges(int slot) {
		Arrangement mirrorAcross = this.STORAGE[slot];
		float deltaX = this.x - mirrorAcross.x;
		float deltaY = this.y - mirrorAcross.y;
		float deltaZ = this.z - mirrorAcross.z;
		float deltaPitch = this.pitch - mirrorAcross.pitch;
		float deltaYaw = this.yaw - mirrorAcross.yaw;
		float deltaRoll = this.roll - mirrorAcross.roll;
		this.setPos(mirrorAcross.x - deltaX, mirrorAcross.y - deltaY, mirrorAcross.z - deltaZ);
		this.setAngles(mirrorAcross.pitch - deltaPitch, mirrorAcross.yaw - deltaYaw, mirrorAcross.roll - deltaRoll);
	}

	public void scaleTranslations(float horizontalScale, float verticalScale) {
		Arrangement beforeAnimation = this.STORAGE[BEFORE_CFA_ANIMATIONS];
		this.setPos(
				beforeAnimation.x + (this.x - beforeAnimation.x) * horizontalScale,
				beforeAnimation.y + (this.y - beforeAnimation.y) * verticalScale,
				beforeAnimation.z + (this.z - beforeAnimation.z) * horizontalScale
		);
	}

	public void multiplyAngles(float factor) {
		this.pitch *= factor; this.roll *= factor; this.yaw *= factor;
	}

	public void fullyMirror() {
		this.x *= -1; this.yaw *= -1; this.roll *= -1;
	}

	private void lerpPos(float delta, @NotNull Arrangement from, @NotNull Arrangement to) {
		this.setPos(
				MathHelper.lerp(delta, from.x, to.x),
				MathHelper.lerp(delta, from.y, to.y),
				MathHelper.lerp(delta, from.z, to.z)
		);
	}

	public void lerp(float delta, Arrangement from, Arrangement to) {
		if(from == null || to == null) return;
		this.lerpPos(delta, from, to);
		this.setAngles(
				MathHelper.lerp(delta, from.pitch, to.pitch),
				MathHelper.lerp(delta, from.yaw, to.yaw),
				MathHelper.lerp(delta, from.roll, to.roll)
		);
	}

	public void wrappedLerpRadians(float delta, Arrangement from, Arrangement to) {
		if(from == null || to == null) return;
		this.lerpPos(delta, from, to);
		this.setAngles(
				wrappedLerpRadians(delta, from.pitch, to.pitch),
				wrappedLerpRadians(delta, from.yaw, to.yaw),
				wrappedLerpRadians(delta, from.roll, to.roll)
		);
	}

	public void wrappedLerpDegrees(float delta, Arrangement from, Arrangement to) {
		if(from == null || to == null) return;
		this.lerpPos(delta, from, to);
		this.setAngles(
				wrappedLerpDegrees(delta, from.pitch, to.pitch),
				wrappedLerpDegrees(delta, from.yaw, to.yaw),
				wrappedLerpDegrees(delta, from.roll, to.roll)
		);
	}

	public static float wrapRadians(float radians) {
		float wrapped = radians % TAU;
		if(wrapped >= PI) wrapped -= TAU;
		if(wrapped < -PI) wrapped += TAU;
		return wrapped;
	}

	private static float wrapDegrees(float radians) {
		float wrapped = radians % 360;
		if(wrapped >= 180) wrapped -= 360;
		if(wrapped < -180) wrapped += 360;
		return wrapped;
	}

	private static float wrappedLerpRadians(float delta, float start, float end) {
		return start + wrapRadians(end - start) * delta;
	}

	private static float wrappedLerpDegrees(float delta, float start, float end) {
		return start + wrapDegrees(end - start) * delta;
	}
}
