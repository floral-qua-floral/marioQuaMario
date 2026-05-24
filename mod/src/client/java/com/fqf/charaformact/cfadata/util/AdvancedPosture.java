package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Posture;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedPosture extends Posture {
	private final AdvancedArrangement[] ARRANGEMENTS;
	private boolean isDegrees;

	public AdvancedPosture(AdvancedArrangement[] arrangements) {
		super(arrangements);
		this.ARRANGEMENTS = arrangements;
	}

	public static AdvancedPosture from(PlayerEntityModel<?> model) {
		@Nullable ModelPart tail, rightEar, leftEar;
		if(model instanceof AppearanceModel appearanceModel) {
			tail = appearanceModel.tail; rightEar = appearanceModel.rightEar; leftEar = appearanceModel.leftEar;
		}
		else {
			tail = null; rightEar = null; leftEar = null;
		}
		return new AdvancedPosture(new AdvancedArrangement[]{
				new AdvancedArrangement(), // everything
				AdvancedArrangement.of(model.head),
				AdvancedArrangement.of(model.body),
				AdvancedArrangement.of(model.rightArm),
				AdvancedArrangement.of(model.leftArm),
				AdvancedArrangement.of(model.rightLeg),
				AdvancedArrangement.of(model.leftLeg),
				AdvancedArrangement.of(tail),
				AdvancedArrangement.of(rightEar),
				AdvancedArrangement.of(leftEar)
		});
	}

	public static AdvancedPosture from(AdvancedPosture original) {
		AdvancedArrangement[] newArrangements = new AdvancedArrangement[original.ARRANGEMENTS.length];
		for (int index = 0; index < original.ARRANGEMENTS.length; index++) {
			AdvancedArrangement copying = original.ARRANGEMENTS[index];
			if(copying == null) newArrangements[index] = null;
			else {
				AdvancedArrangement writing = new AdvancedArrangement();
				newArrangements[index] = writing;
				writing.setPos(copying.x, copying.y, copying.z);
				writing.setAngles(copying.pitch, copying.yaw, copying.roll);
			}
		}
		return new AdvancedPosture(newArrangements);
	}

	public void store(int slot) {
		for(AdvancedArrangement arrangement : this.ARRANGEMENTS) {
			if(arrangement == null) continue;
			arrangement.store(slot);
		}
	}

	public void fullyMirror() {
		this.EVERYTHING.x *= -1; this.EVERYTHING.yaw *= -1; this.EVERYTHING.roll *= -1;
		this.HEAD.x *= -1; this.HEAD.yaw *= -1; this.HEAD.roll *= -1;
		this.TORSO.x *= -1; this.TORSO.yaw *= -1; this.TORSO.roll *= -1;

		Arrangement rightArmCopy = new Arrangement();
		rightArmCopy.setPos(this.RIGHT_ARM.x, this.RIGHT_ARM.y, this.RIGHT_ARM.z);
		rightArmCopy.setAngles(this.RIGHT_ARM.pitch, this.RIGHT_ARM.yaw, this.RIGHT_ARM.roll);
		Arrangement rightLegCopy = new Arrangement();
		rightLegCopy.setPos(this.RIGHT_LEG.x, this.RIGHT_LEG.y, this.RIGHT_LEG.z);
		rightLegCopy.setAngles(this.RIGHT_LEG.pitch, this.RIGHT_LEG.yaw, this.RIGHT_LEG.roll);

		this.RIGHT_ARM.setPos(-this.LEFT_ARM.x, this.LEFT_ARM.y, this.LEFT_ARM.z);
		this.RIGHT_ARM.setAngles(this.LEFT_ARM.pitch, -this.LEFT_ARM.yaw, -this.LEFT_ARM.roll);
		this.LEFT_ARM.setPos(-rightArmCopy.x, rightArmCopy.y, rightArmCopy.z);
		this.LEFT_ARM.setAngles(rightArmCopy.pitch, -rightArmCopy.yaw, -rightArmCopy.roll);

		this.RIGHT_LEG.setPos(-this.LEFT_LEG.x, this.LEFT_LEG.y, this.LEFT_LEG.z);
		this.RIGHT_LEG.setAngles(this.LEFT_LEG.pitch, -this.LEFT_LEG.yaw, -this.LEFT_LEG.roll);
		this.LEFT_LEG.setPos(-rightLegCopy.x, rightLegCopy.y, rightLegCopy.z);
		this.LEFT_LEG.setAngles(rightLegCopy.pitch, -rightLegCopy.yaw, -rightLegCopy.roll);
	}

	public void toDegrees() {
		if(this.isDegrees) return;
		this.isDegrees = true;
		this.multiplyAllAngles(MathHelper.DEGREES_PER_RADIAN);
	}
	public void toRadians() {
		if(!this.isDegrees) return;
		this.isDegrees = false;
		this.multiplyAllAngles(MathHelper.RADIANS_PER_DEGREE);
	}

	private void multiplyAllAngles(float factor) {
		for(AdvancedArrangement arrangement : ARRANGEMENTS) {
			if(arrangement != null) arrangement.multiplyAngles(factor);
		}
	}

	public void apply(PlayerEntityModel<?> model) {
		@Nullable ModelPart tail, rightEar, leftEar;
		if(model instanceof AppearanceModel appearanceModel) {
			tail = appearanceModel.tail; rightEar = appearanceModel.rightEar; leftEar = appearanceModel.leftEar;
		}
		else {
			tail = null; rightEar = null; leftEar = null;
		}
		applyCertain(model.head, this.HEAD); applyCertain(model.body, this.TORSO);
		applyCertain(model.rightArm, this.RIGHT_ARM); applyCertain(model.leftArm, this.LEFT_ARM);
		applyCertain(model.rightLeg, this.RIGHT_LEG); applyCertain(model.leftLeg, this.LEFT_LEG);
		apply(tail, this.TAIL); apply(rightEar, this.RIGHT_EAR); apply(leftEar, this.LEFT_EAR);
	}

	private static void apply(@Nullable ModelPart part, @Nullable Arrangement arrangement) {
		if(part == null || arrangement == null) return;
		applyCertain(part, arrangement);
	}

	private static void applyCertain(@NotNull ModelPart part, @NotNull Arrangement arrangement) {
		part.setPivot(arrangement.x, arrangement.y, arrangement.z);
		part.setAngles(arrangement.pitch, arrangement.yaw, arrangement.roll);
	}

	public void lerp(float delta, AdvancedPosture from, AdvancedPosture to) {
		for(int index = 0; index < this.ARRANGEMENTS.length; index++) {
			AdvancedArrangement mutating = this.ARRANGEMENTS[index];
			if(mutating != null) mutating.lerp(delta, from.ARRANGEMENTS[index], to.ARRANGEMENTS[index]);
		}
	}

	public void wrappedLerp(float delta, AdvancedPosture from, AdvancedPosture to) {
		if(this.isDegrees) this.wrappedLerpDegrees(delta, from, to);
		else this.wrappedLerpRadians(delta, from, to);
	}

	private void wrappedLerpRadians(float delta, AdvancedPosture from, AdvancedPosture to) {
		for(int index = 0; index < this.ARRANGEMENTS.length; index++) {
			AdvancedArrangement mutating = this.ARRANGEMENTS[index];
			if(mutating != null) mutating.wrappedLerpRadians(delta, from.ARRANGEMENTS[index], to.ARRANGEMENTS[index]);
		}
	}

	private void wrappedLerpDegrees(float delta, AdvancedPosture from, AdvancedPosture to) {
		for(int index = 0; index < this.ARRANGEMENTS.length; index++) {
			AdvancedArrangement mutating = this.ARRANGEMENTS[index];
			if(mutating != null) mutating.wrappedLerpDegrees(delta, from.ARRANGEMENTS[index], to.ARRANGEMENTS[index]);
		}
	}
}
