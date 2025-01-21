package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.BodyPartAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.mariodata.util.ArrangementSet;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;

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

	@Unique
	public static void setupArrangement(ModelPart from, Arrangement to) {
		to.x = from.pivotX; to.y = from.pivotY; to.z = from.pivotZ;
		to.pitch = from.pitch; to.yaw = from.yaw; to.roll = from.roll;
	}

	@Unique
	public static void undoFrame(ModelPart part, Arrangement arrangement, float multiplier) {
		part.pivotX += multiplier * arrangement.x;
		part.pivotY += multiplier * arrangement.y;
		part.pivotZ += multiplier * arrangement.z;
		part.pitch += multiplier * arrangement.pitch;
		part.yaw += multiplier * arrangement.yaw;
		part.roll += multiplier * arrangement.roll;
	}

	@Unique
	private static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, Arrangement.Mutator mutator, float progress, boolean isMirrored) {
		if(mutator == null) return;
		setupArrangement(part, arrangement);
		applyMutator(data, arrangement, mutator, progress, isMirrored);
	}

	@Unique
	public static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, BodyPartAnimation animation, float progress, boolean isMirrored) {
		if(animation == null) setupArrangement(part, arrangement);
		else animatePart(data, part, arrangement, animation.mutator(), progress, isMirrored);
	}

	@Unique
	public static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, LimbAnimation animation, float progress, boolean isMirrored) {
		if(animation == null || animation.mutator() == null) setupArrangement(part, arrangement);
		else animatePart(data, part, arrangement, animation.mutator(), progress, isMirrored);
	}

	@Unique
	public static void applyMutator(MarioPlayerData data, Arrangement arrangement, Arrangement.Mutator mutator, float progress, boolean isMirrored) {
		float factor = MathHelper.DEGREES_PER_RADIAN;
		arrangement.setAngles(arrangement.pitch * factor, arrangement.yaw * factor, arrangement.roll * factor);

		float unmutatedX = arrangement.x;
		float unmutatedYaw = arrangement.yaw;
		mutator.mutate(data, arrangement, progress);
		if(isMirrored) {
			arrangement.x -= 2 * (arrangement.x - unmutatedX);
			arrangement.yaw -= 2 * (arrangement.yaw - unmutatedYaw);
		}

		factor = MathHelper.RADIANS_PER_DEGREE;
		arrangement.setAngles(arrangement.pitch * factor, arrangement.yaw * factor, arrangement.roll * factor);
	}

	@Unique
	public static boolean isArmBusy(BipedEntityModel.ArmPose armPose, BipedEntityModel.ArmPose otherArmPose) {
		return armPose.isTwoHanded() || otherArmPose.isTwoHanded() || switch (armPose) {
			case EMPTY, ITEM -> false;
			case BLOCK, BOW_AND_ARROW, THROW_SPEAR, CROSSBOW_CHARGE, CROSSBOW_HOLD, SPYGLASS, TOOT_HORN, BRUSH -> true;
		};
	}
}
