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

	public void mirrorChanges(int slot) {
		Arrangement rightArmDeltas = ((AdvancedArrangement) this.RIGHT_ARM).getDeltas(slot);
		Arrangement leftArmDeltas = ((AdvancedArrangement) this.LEFT_ARM).getDeltas(slot);
		Arrangement rightLegDeltas = ((AdvancedArrangement) this.RIGHT_LEG).getDeltas(slot);
		Arrangement leftLegDeltas = ((AdvancedArrangement) this.LEFT_LEG).getDeltas(slot);
		((AdvancedArrangement) this.RIGHT_ARM).resetTo(slot); ((AdvancedArrangement) this.LEFT_ARM).resetTo(slot);
		((AdvancedArrangement) this.RIGHT_LEG).resetTo(slot); ((AdvancedArrangement) this.LEFT_LEG).resetTo(slot);

		this.RIGHT_ARM.addPos(-leftArmDeltas.x, leftArmDeltas.y, leftArmDeltas.z);
		this.RIGHT_ARM.addAngles(leftArmDeltas.pitch, -leftArmDeltas.yaw, leftArmDeltas.roll);
		this.LEFT_ARM.addPos(-rightArmDeltas.x, rightArmDeltas.y, rightArmDeltas.z);
		this.LEFT_ARM.addAngles(rightArmDeltas.pitch, -rightArmDeltas.yaw, rightArmDeltas.roll);
		this.RIGHT_LEG.addPos(-leftLegDeltas.x, leftLegDeltas.y, leftLegDeltas.z);
		this.RIGHT_LEG.addAngles(leftLegDeltas.pitch, -leftLegDeltas.yaw, leftLegDeltas.roll);
		this.LEFT_LEG.addPos(-rightLegDeltas.x, rightLegDeltas.y, rightLegDeltas.z);
		this.LEFT_LEG.addAngles(rightLegDeltas.pitch, -rightLegDeltas.yaw, rightLegDeltas.roll);
	}

	public void swapSidedParts() {
		// TODO: Try just swapping the variables around instead of making new Arrangements

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

	public void mirrorNonSidedChanges(int slot) {
		// TODO: Implement head, torso, & tail mirroring
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

	public void lerp(AdvancedPosture from, AdvancedPosture to, float delta) {
		for(int index = 0; index < this.ARRANGEMENTS.length; index++) {
			AdvancedArrangement mutating = this.ARRANGEMENTS[index];
			AdvancedArrangement fromArrangement = from.ARRANGEMENTS[index];
			AdvancedArrangement toArrangement = to.ARRANGEMENTS[index];
			if(mutating == null || fromArrangement == null || toArrangement == null) continue;
			mutating.setPos(
					MathHelper.lerp(delta, fromArrangement.x, toArrangement.x),
					MathHelper.lerp(delta, fromArrangement.y, toArrangement.y),
					MathHelper.lerp(delta, fromArrangement.z, toArrangement.z)
			);
			mutating.setAngles(
					MathHelper.lerp(delta, fromArrangement.pitch, toArrangement.pitch),
					MathHelper.lerp(delta, fromArrangement.yaw, toArrangement.yaw),
					MathHelper.lerp(delta, fromArrangement.roll, toArrangement.roll)
			);
		}
	}
}
